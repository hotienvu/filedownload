package com.vho.filedownload.downloader;

import java.io.File;

public class FtpDownloader implements Downloader {
  public FtpDownloader() {
  }

  @Override
  public File download(DownloadTask task) throws DownloadTask.FileDownloadException {
    return null;
  }

  @Override
  public void close() throws Exception {

  }
}
