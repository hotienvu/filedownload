package com.vho.filedownload.downloader;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Downloaders {

  private static final Logger LOG = LoggerFactory.getLogger(Downloaders.class);

  private static final Map<String, String> defaultDownloaderClasses = new HashMap<String, String>() {{
    put("http", HttpDownloader.class.getCanonicalName());
    put("ftp", FtpDownloader.class.getCanonicalName());
  }};

  /**
   * initialize a Downloader from a URL scheme
   * @param scheme a scheme in short or long form e.g. 'http' or 'com.custom.scheme'
   * @return corresponding downloader
   */
  public static Downloader fromScheme(String scheme) throws UnsupportedURLException {
    String clazz = defaultDownloaderClasses.getOrDefault(scheme, scheme + ".DefaultDownloader");
    try {
      return (Downloader) Class.forName(clazz).getConstructor().newInstance();
    } catch (InstantiationException |
              IllegalAccessException |
              InvocationTargetException |
              NoSuchMethodException |
              ClassNotFoundException e) {
      LOG.error("Failed to initialize Downloader from URL scheme" + scheme, e);
      throw new UnsupportedURLException("Unsupported URL scheme", e);
    }
  }

  public static class UnsupportedURLException extends Exception {
    UnsupportedURLException(String s, Throwable e) {
      super(s, e);
    }
  }
}
