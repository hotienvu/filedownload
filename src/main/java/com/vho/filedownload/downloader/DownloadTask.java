package com.vho.filedownload.task;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.vho.filedownload.Utils.checkNotNull;

public abstract class DownloadTask implements Callable<File>, Downloadable {

  public static String DOWNLOAD_TMP_DIR_OPT_KEY = "download.dir.tmp";
  public static String DOWNLOAD_TMP_DIR_OPT_DEFAULT_VAL = "/tmp";

  public static String DOWNLOAD_TARGET_DIR_OPT_KEY = "download.dir.target";
  public static String DOWNLOAD_TARGET_DIR_OPT_DEFAULT_VAL = "/tmp";

  public static String DOWNLOAD_URL_SCHEME_OPT_KEY = "download.url.scheme";

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
  public static DownloadTaskBuilder fromURL(String url) {
    return new DownloadTaskBuilder(url);
  }

  public static class DownloadTaskBuilder {
    private Map<String, String> options;
    private final String url;

    private DownloadTaskBuilder(String url) {
      this.url = url;
      this.options = new HashMap<String, String>() {{
        put( DOWNLOAD_TMP_DIR_OPT_KEY, DOWNLOAD_TMP_DIR_OPT_DEFAULT_VAL);
      }};
    }


    public DownloadTaskBuilder option(String key, String value) {
      options.put(key, value);
      return this;
    }

    public DownloadTaskBuilder withOptions(Map<String, String> params) {
      for (Map.Entry<String, String> e: params.entrySet()) {
        options.put(e.getKey(), e.getValue());
      }
      return this;
    }


    public DownloadTaskBuilder scheme(String clazz) {
      options.put(DOWNLOAD_URL_SCHEME_OPT_KEY, clazz);
      return this;
    }

    public DownloadTaskBuilder targetDir(String dir) {
      return option(DOWNLOAD_TARGET_DIR_OPT_KEY, dir);
    }

    public DownloadTaskBuilder tmpDir(String dir) {
      return option(DOWNLOAD_TMP_DIR_OPT_KEY, dir);
    }

    /**
     * Factory method for creating the corresponding Download Task based on the URL scheme
     * @return a DownloadTask object
     */
    public DownloadTask create() {
      checkNotNull(options.get(DOWNLOAD_TARGET_DIR_OPT_KEY), "target dir must be specified");
      checkNotNull(options.get(DOWNLOAD_TMP_DIR_OPT_KEY), "target dir must be specified");

      if (url.startsWith("http://")) return new HttpTask(url, options);
      if (url.startsWith("file://")) return new LocalTask(url, options);
      if (url.startsWith("ftp://")) return new FtpTask(url, options);
      throw new IllegalArgumentException("Unknown url scheme");

//      try {
//        URI parsedUrl = new URI(url);
//        String scheme = parsedUrl.getScheme();
//        return getClassForScheme(scheme)
//          .getConstructor(String.class, Map.class)
//          .newInstance(url, options);
//      } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
//               ClassNotFoundException | InvocationTargetException | URISyntaxException e) {
//        e.printStackTrace();
//      }
    }
//
//    private Class<? extends DownloadTask> getClassForScheme(String scheme) throws ClassNotFoundException {
//      switch (scheme) {
//        case "http": return HttpTask.class;
//        default: return Class.forName(options.get(DOWNLOAD_URL_SCHEME_OPT_KEY) + ".Default");
//      }
//    }
  }



  public static class FileDownloadException extends Exception {
    public FileDownloadException(String msg, Throwable cause) {
      super(msg, cause);
    }
  }
}
