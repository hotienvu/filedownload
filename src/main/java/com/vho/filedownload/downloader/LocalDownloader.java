package com.vho.filedownload.downloader;

import java.io.File;
import java.util.Map;

public class FileDownloader implements Downloader {
  public FileDownloader() {
  }

  @Override
  public File download(String url, Map<String, String> options) throws DownloadTask.FileDownloadException {
    return null;
  }
}
