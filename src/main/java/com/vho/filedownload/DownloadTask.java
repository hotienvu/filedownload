package com.vho.filedownload;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.vho.filedownload.Utils.checkNotNull;

abstract class DownloadTask implements Callable<File>, Downloadable {

  static String DOWNLOAD_TMP_DIR_OPT_KEY = "download.dir.tmp";
  static String DOWNLOAD_TMP_DIR_OPT_DEFAULT_VAL = "/tmp";

  static String DOWNLOAD_TARGET_DIR_OPT_KEY = "download.dir.target";
  static String DOWNLOAD_TARGET_DIR_OPT_DEFAULT_VAL = "/tmp";

  /**
   * Usage: task.fromURL("hdfs://example.com/filename")
   *            .targetDir("/target")
   *            .tmpDir("/tmp")
   *            .scheme("com.vho.custom.scheme.hdfs")
   *            .option("key", "value")
   *            .create();
   * @param url the URL to download
   * @return the DownloadTask builder object
   */
  static DownloadTaskBuilder fromURL(String url) {
    return new DownloadTaskBuilder(url);
  }

  static class DownloadTaskBuilder {
    private Map<String, String> options;
    private final String url;

    private DownloadTaskBuilder(String url) {
      this.url = url;
      this.options = new HashMap<String, String>() {{
        put( DOWNLOAD_TMP_DIR_OPT_KEY, DOWNLOAD_TMP_DIR_OPT_DEFAULT_VAL);
      }};
    }


    DownloadTaskBuilder option(String key, String value) {
      options.put(key, value);
      return this;
    }

    DownloadTaskBuilder targetDir(String dir) {
      return option(DOWNLOAD_TARGET_DIR_OPT_KEY, dir);
    }

    DownloadTaskBuilder tmpDir(String dir) {
      return option(DOWNLOAD_TMP_DIR_OPT_KEY, dir);
    }

    /**
     * Factory method for creating the corresponding Download Task based on the URL scheme
     * @return a DownloadTask object
     */
    DownloadTask create() {
      checkNotNull(options.get(DOWNLOAD_TARGET_DIR_OPT_KEY), "target dir must be specified");
      checkNotNull(options.get(DOWNLOAD_TMP_DIR_OPT_KEY), "target dir must be specified");

      if (url.startsWith("http://"))
        return new DownloadHttp(url, options);
      else if (url.startsWith("file://"))
        return new DownloadLocal(url, options);
      else if (url.startsWith("ftp://"))
        return new DownloadFtp(url, options);
      else throw new IllegalArgumentException("Unknown url scheme");
    }
  }

  static class DownloadLocal extends DownloadTask {
    private final String url;
    private final Map<String, String> options;

    private DownloadLocal(String url, Map<String, String> options) {
      this.url = url;
      this.options = options;
    }

    @Override
    public String getTargetDir() {
      return options.get(DOWNLOAD_TARGET_DIR_OPT_KEY);
    }

    @Override
    public String getURL() {
      return url;
    }

    @Override
    public File call() throws Exception {
      return null;
    }
  }

  static class DownloadHttp extends DownloadTask {
    static final String DOWNLOAD_HTTP_BUFFER_SIZE_OPT_KEY = "download.http.buffer.size.bytes";
    static final String DOWNLOAD_HTTP_BUFFER_SIZE_OPT_DEFAULT_VAL = "4096";

    private final String url;
    private final Map<String, String> options;

    private DownloadHttp(String url, Map<String, String> options) {
      this.url = url;
      this.options = options;
    }

    @Override
    public String getTargetDir() {
      return options.get(DOWNLOAD_TARGET_DIR_OPT_KEY);
    }

    @Override
    public String getURL() {
      return url;
    }

    @Override
    public File call() throws FileDownloadException {
      String tmpDir = checkNotNull(options.get(DOWNLOAD_TMP_DIR_OPT_KEY));
      String targetDir = checkNotNull(options.get(DOWNLOAD_TARGET_DIR_OPT_KEY));

      URI reqURL;
      String fileName;
      try {
        reqURL = new URI(url);
        fileName = Paths.get(reqURL.getPath()).getFileName().toString();
      } catch (URISyntaxException | NullPointerException e) {
        throw new FileDownloadException("Failed to parse " + url, e);
      }

      CloseableHttpClient client = HttpClients.createDefault();
      HttpUriRequest getReq = new HttpGet(reqURL);

      final File tmpFile = Paths.get(tmpDir, fileName + ".download").toFile();
      tmpFile.deleteOnExit();
      try (CloseableHttpResponse response = client.execute(getReq);
           InputStream is = response.getEntity().getContent();
           OutputStream os = new FileOutputStream(tmpFile)) {
        if (response.getStatusLine().getStatusCode() != 200)
          throw new IOException(response.getStatusLine().toString());

        String bufferSizeStr = options.getOrDefault(
          DOWNLOAD_HTTP_BUFFER_SIZE_OPT_KEY, DOWNLOAD_HTTP_BUFFER_SIZE_OPT_DEFAULT_VAL);
        int bufferSize = Integer.parseInt(bufferSizeStr);
        byte[] buffer = new byte[bufferSize];
        int totalRead = 0, read;
        while ((read = is.read(buffer, 0, bufferSize)) > 0) {
          totalRead += read;
          os.write(buffer, 0, read);
        }

        File result = Paths.get(targetDir, fileName).toFile();
        if (tmpFile.renameTo(result)) {
          System.out.println("Successfully downloaded to " + result.getAbsolutePath());
          return result;
        } else {
          throw new FileSystemException("Failed to save to " + result.getAbsolutePath());
        }
      } catch (IOException e) {
        throw new FileDownloadException("Failed to download from: " + url + "Cause: " + e.getMessage(), e);
      }
    }
  }

  static class DownloadFtp extends DownloadTask {
    private final String url;
    private final Map<String, String> options;

    DownloadFtp(String url, Map<String, String> options) {
      this.url = url;
      this.options = options;
    }

    @Override
    public String getTargetDir() {
      return options.get(DOWNLOAD_TARGET_DIR_OPT_KEY);
    }

    @Override
    public String getURL() {
      return url;
    }

    @Override
    public File call() throws Exception {
      return null;
    }
  }

  class FileDownloadException extends Exception {
    FileDownloadException(String msg, Throwable cause) {
      super(msg, cause);
    }
  }
}
