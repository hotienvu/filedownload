package com.vho.filedownload;

import com.vho.filedownload.downloader.DownloadTask;
import com.vho.filedownload.downloader.Downloader;
import com.vho.filedownload.downloader.Downloaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class DownloadManager {

  private static final Logger LOG = LoggerFactory.getLogger(DownloadManager.class);
  /**
   * Use a dedicated thread pool for Downloading tasks which are IO-heavy
   * so it wont affect other computations
   *
   */
  private final WorkerPool workerPool;
  private volatile boolean terminated;
  private String tmpDir;
  private String targetDir;

  // maximum one file downloader per URL scheme
  private ConcurrentMap<String, Downloader> downloaders;

  DownloadManager(WorkerPool pool) {
    this.workerPool = pool;
    this.terminated = false;
    this.targetDir = DownloadTask.DOWNLOAD_TARGET_DIR_OPT_DEFAULT_VAL;
    this.tmpDir = DownloadTask.DOWNLOAD_TMP_DIR_OPT_DEFAULT_VAL;
    this.downloaders = new ConcurrentHashMap<>();
  }


  public void awaitTermination() {
    while (!terminated) {
    }
    LOG.info("Download Manager stopped");
  }

  public String getTargetDir() {
    return targetDir;
  }

  public void setTargetDir(String targetDir) {
    this.targetDir = targetDir;
  }

  public String getTmpDir() {
    return tmpDir;
  }

  public void setTmpDir(String tmpDir) {
    this.tmpDir = tmpDir;
  }

  /**
   * Download list of URLs with options
   * @param urls list of URL to download
   * @param options the settings map
   * @return list of downloaded files
   */
  public CompletableFuture<List<File>> download(List<String> urls, Map<String, String> options) {
    List<CompletableFuture<File>> files = urls.stream().map(u -> download(u, options)).collect(Collectors.toList());
    return Utils.sequence(files);
  }

  /**
   * Download a single URL with options
   * @param url list of
   * @param options the settings map
   * @return downloaded file
   */
  public CompletableFuture<File> download(String url, Map<String, String> options) {
    try {
      DownloadTask task = DownloadTask.fromURL(url)
        .tmpDir(tmpDir)
        .options(options)
        .targetDir(targetDir)
        .create();

      String scheme = task.getScheme();
      Downloader downloader = downloaders.getOrDefault(scheme, Downloaders.fromScheme(scheme));
      downloaders.putIfAbsent(scheme, downloader);

      return workerPool.submit(() -> downloader.download(task));
    } catch (Downloaders.UnsupportedURLException | URISyntaxException e) {
      return Utils.failedFuture(e);
    }
  }

  /**
   * gracefully shutdown the underlying worker pool
   * @param timeout max wait for worker pool to shutdown
   * @param unit timeout unit
   */
  synchronized
  public void shutdown(long timeout, TimeUnit unit) {
    if (!terminated) {
      LOG.info("Closing all downloaders");
      for (Downloader d: downloaders.values()) {
        try {
          d.close();
        } catch (Exception e) {
          LOG.error("Failed to close downloader ", e);
        }
      }

      LOG.info("Shutting down Download Manager...");
      terminated = true;
      workerPool.shutdown(timeout, unit);
      LOG.info("Download Manager stopped.");
    }
  }
}
