package com.vho.filedownload.downloader.hdfs;

import com.vho.filedownload.downloader.DownloadTask;
import com.vho.filedownload.downloader.Downloader;

import java.io.File;

public class DefaultDownloader implements Downloader {
  @Override
  public File download(DownloadTask task) throws DownloadTask.FileDownloadException {
    return null;
  }
}
