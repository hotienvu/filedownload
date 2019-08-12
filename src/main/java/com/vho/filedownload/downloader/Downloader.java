package com.vho.filedownload.downloader;

import java.io.File;

public interface Downloader {

  File download(DownloadTask task) throws DownloadTask.FileDownloadException;
}
