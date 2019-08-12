package com.vho.filedownload.downloader.local;

import com.vho.filedownload.downloader.DownloadTask;
import com.vho.filedownload.downloader.Downloader;
import com.vho.filedownload.downloader.HttpDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DefaultDownloader implements Downloader {
  private static final Logger LOG = LoggerFactory.getLogger(HttpDownloader.class);

  public DefaultDownloader() {
  }

  @Override
  public File download(DownloadTask task) throws DownloadTask.FileDownloadException {
    LOG.info("Downloading {}...", task);
    try {
      String fileName = Paths.get(task.getUrl().getPath()).getFileName().toString();
      final Path target = Paths.get(task.getTargetDir(), fileName);
      Files.copy(Paths.get(task.getUrl()), target, StandardCopyOption.REPLACE_EXISTING);
      LOG.info("Successfully downloaded to " + target.toString());
      return new File(target.toUri());
    } catch (IOException e) {
      throw new DownloadTask.FileDownloadException("Failed to copy file " + task.getUrl() + " to " + task.getTargetDir(), e);
    }
  }
}
