package com.vho.filedownload.downloader;

import java.io.File;
import java.util.Map;

class FtpTask extends DownloadTask {
  private final String url;
  private final Map<String, String> options;

  FtpTask(String url, Map<String, String> options) {
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
