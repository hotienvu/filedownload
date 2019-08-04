package com.vho;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.vho.Utils.checkNotNull;

abstract class DownloadTask implements Callable<File> {

  static String DOWNLOAD_TMP_DIR_OPT_KEY = "download.dir.tmp";
  static String DOWNLOAD_TARGET_DIR_OPT_KEY = "download.dir.target";

  static DownloadTaskQuery fromURL(String url) {
    return new DownloadTaskQuery(url);
  }

  static class DownloadTaskQuery {
    private Map<String, String> options;
    private final String url;

    private DownloadTaskQuery(String url) {
      this.url = url;
    }


    DownloadTaskQuery option(String key, String value) {
      options.put(key, value);
      return this;
    }

    DownloadTaskQuery targetDir(String dir) {
      return option(DOWNLOAD_TARGET_DIR_OPT_KEY, dir);
    }

    DownloadTaskQuery tmpDir(String dir) {
      return option(DOWNLOAD_TMP_DIR_OPT_KEY, dir);
    }

    DownloadTask create() {
      checkNotNull(options.get(DOWNLOAD_TMP_DIR_OPT_KEY), "tmp download dir must be specified");
      checkNotNull(options.get(DOWNLOAD_TARGET_DIR_OPT_KEY), "target dir must be specified");

      if (url.startsWith("http://"))
        return new DownloadHttp(url, options);
      else if (url.startsWith("file://"))
        return new DownloadLocal(url, options);
      else throw new IllegalArgumentException("Unrecognized url scheme");
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
    public File call() throws Exception {
      return null;
    }
  }

  static class DownloadHttp extends DownloadTask {
    static final String DOWNLOAD_HTTP_BUFFER_SIZE_OPT_KEY = "download.http.buffer.size.bytes";
    static final String DOWNLOAD_HTTP_BUFFER_SIZE_DEFAULT_VAL = "1024";

    private final String url;
    private final Map<String, String> options;

    private DownloadHttp(String url, Map<String, String> options) {
      this.url = url;
      this.options = options;
    }

    @Override
    public File call() throws FileDownloadException {
      String tmpDir = checkNotNull(options.get("tmpDir"));
      String targetDir = checkNotNull(options.get("targetDir"));

      URI reqURL = null;
      try {
        reqURL = new URI(url);
      } catch (URISyntaxException e) {
        throw new FileDownloadException("Failed to parse " + url, e);
      }
      String fileName = Paths.get(reqURL.getPath()).getFileName().toString();
      CloseableHttpClient client = HttpClients.createDefault();
      HttpUriRequest getReq = new HttpGet(reqURL);

      final File tmpFile = Paths.get(tmpDir, fileName + ".download").toFile();
      try (CloseableHttpResponse response = client.execute(getReq);
           InputStream is = response.getEntity().getContent();
           OutputStream os = new FileOutputStream(tmpFile)) {

        String bufferSizeStr = options.getOrDefault(
          DOWNLOAD_HTTP_BUFFER_SIZE_OPT_KEY, DOWNLOAD_HTTP_BUFFER_SIZE_DEFAULT_VAL);
        int bufferSize = Integer.parseInt(bufferSizeStr);
        byte[] buffer = new byte[bufferSize];
        int totalRead = 0, read;
        while ((read = is.read(buffer, 0, bufferSize)) > 0) {
          totalRead += read;
          os.write(buffer, 0, read);
        }

        File result = Paths.get(targetDir, fileName).toFile();
        if (tmpFile.renameTo(result)) {
          return result;
        } else {
          throw new FileSystemException("Failed to save to " + result.getAbsolutePath());
        }
      } catch (IOException e) {
        throw new FileDownloadException("Failed to download from " + url, e);
      }
    }
  }



  private class FileDownloadException extends Exception {
    FileDownloadException(String msg, Throwable cause) {
      super(msg, cause);
    }
  }
}
