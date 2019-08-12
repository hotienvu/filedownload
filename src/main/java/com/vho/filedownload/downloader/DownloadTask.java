package com.vho.filedownload.downloader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static com.vho.filedownload.Utils.checkNotNull;

public class DownloadTask {

  public static String DOWNLOAD_TMP_DIR_OPT_DEFAULT_VAL = "/tmp";
  public static String DOWNLOAD_TARGET_DIR_OPT_DEFAULT_VAL = "/tmp";

  /**
   * downloader classpath for custom scheme e.g.
   * --conf download.url.scheme.hdfs=com.vho.filedownload.downloader.hdfs
   * This will load com.vho.filedownload.downloader.hdfs.DefaultDownloader
   */
  public static String DOWNLOAD_URL_SCHEME_PREFIX_OPT_KEY = "download.url.scheme.";

  private final URI url;
  private final String targetDir;
  private final String tmpDir;
  private final Map<String, String> options;


  private DownloadTask(URI url, String targetDir, String tmpDir, Map<String, String> options) {
    this.url = url;
    this.targetDir = targetDir;
    this.tmpDir = tmpDir;
    this.options = options;
  }

  // GETTER(s)
  public URI getUrl() {
    return url;
  }

  public String getTargetDir() {
    return targetDir;
  }

  public String getTmpDir() {
    return tmpDir;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public String getScheme() {
    String urlScheme = url.getScheme();
    return options.getOrDefault(DOWNLOAD_URL_SCHEME_PREFIX_OPT_KEY + urlScheme, urlScheme);
  }

  /**
   * Usage: task.fromURL("hdfs://example.com/filename")
   *            .targetDir("/target")
   *            .tmpDir("/tmp")
   *            .option("download.url.scheme.hdfs", "com.vho.filedownload.hdfs")
   *            .create();
   * @param url the URL to download
   * @return the DownloadTask builder object
   */
  public static DownloadTaskBuilder fromURL(String url) throws URISyntaxException {
    return new DownloadTaskBuilder(new URI(url));
  }

  @Override
  public String toString() {
    return "DownloadTask{" +
      "url=" + url +
      ", targetDir='" + targetDir + '\'' +
      ", tmpDir='" + tmpDir + '\'' +
      ", options=" + options +
      '}';
  }

  public static class DownloadTaskBuilder {
    private Map<String, String> options;
    private String tmpDir;
    private String targetDir;
    private final URI url;

    private DownloadTaskBuilder(URI url) {
      this.url = url;
      this.options = new HashMap<>();
    }


    public DownloadTaskBuilder option(String key, String value) {
      options.put(key, value);
      return this;
    }

    public DownloadTaskBuilder options(Map<String, String> params) {
      for (Map.Entry<String, String> e: params.entrySet()) {
        options.put(e.getKey(), e.getValue());
      }
      return this;
    }


    public DownloadTaskBuilder targetDir(String dir) {
      targetDir = dir;
      return this;
    }

    public DownloadTaskBuilder tmpDir(String dir) {
      tmpDir = dir;
      return this;
    }

    /**
     * Factory method for creating the corresponding Download Task based on the URL scheme
     * @return a DownloadTask object
     */
    public DownloadTask create() {
      checkNotNull(url, "url must not be null");
      checkNotNull(targetDir, "target dir must not be null");
      checkNotNull(tmpDir, "tmp dir must not be null");

      return new DownloadTask(url, targetDir, tmpDir, options);
    }
  }

  public static class FileDownloadException extends Exception {
    public FileDownloadException(String msg, Throwable cause) {
      super(msg, cause);
    }
  }
}
