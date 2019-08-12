package com.vho.filedownload.downloader;

import java.io.File;
import java.util.Map;

public interface Downloader {

  File download(DownloadTask task) throws DownloadTask.FileDownloadException;
}
