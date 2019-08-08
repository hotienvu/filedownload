package com.vho.filedownload;

import java.io.Serializable;
import java.net.URL;

public class DownloadPart implements Serializable {
  private URL url;
  private long begin;
  private long end;

  /**
   *
   * @param url   URL to download
   * @param begin start byte to download
   * @param end   end byte to download, exclusive
   */
  public DownloadPart(URL url, long begin, long end) {
    this.url = url;
    this.begin = begin;
    this.end = end;
  }
}
