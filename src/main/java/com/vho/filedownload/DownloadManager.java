package com.vho.filedownload;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class DownloadManager {
  private final int numWorkers;

  /**
   * Use a dedicated thread pool for Downloading tasks which are IO-heavy
   * so it wont affect other computations
   *
   */
  private WorkerPool workerPool;
  private volatile boolean terminated;

  DownloadManager(int numWorkers) {
    this.numWorkers = numWorkers;
    this.terminated = false;
    init();
  }

  private void init() {
    workerPool = new WorkerPool(numWorkers);
  }

  synchronized
  public void shutdown(long timeout, TimeUnit unit) {
    if (!terminated) {
      terminated = true;
      workerPool.shutdown(timeout, unit);
    }
  }

  public void awaitTermination() {
    while (!terminated) {
    }
  }

  public CompletableFuture<File> download(String url) {
    return download(url, DownloadTask.DOWNLOAD_TARGET_DIR_OPT_DEFAULT_VAL,
      DownloadTask.DOWNLOAD_TMP_DIR_OPT_DEFAULT_VAL);
  }

  public CompletableFuture<File> download(String url, String targetDir) {
    return download(url, targetDir, DownloadTask.DOWNLOAD_TMP_DIR_OPT_DEFAULT_VAL);
  }

  public CompletableFuture<File> download(String url, String targetDir, String tmpDir) {
    return workerPool.submit(DownloadTask.fromURL(url)
      .targetDir(targetDir)
      .tmpDir(tmpDir)
      .create());
  }

  public CompletableFuture<List<File>> download(List<String> urls) {
    List<CompletableFuture<File>> files = urls.stream().map(this::download).collect(Collectors.toList());
    return Utils.sequence(files);
  }


}
