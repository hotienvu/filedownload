package com.vho.filedownload.downloader;

import java.io.File;

public interface Downloader extends AutoCloseable {

  File download(DownloadTask task) throws DownloadTask.FileDownloadException;
}
