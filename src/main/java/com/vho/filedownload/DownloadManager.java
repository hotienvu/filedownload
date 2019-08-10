package com.vho.filedownload;

import com.vho.filedownload.task.DownloadTask;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class DownloadManager {
  /**
   * Use a dedicated thread pool for Downloading tasks which are IO-heavy
   * so it wont affect other computations
   *
   */
  private final WorkerPool workerPool;
  private volatile boolean terminated;
  private String tmpDir;
  private String targetDir;

  DownloadManager(WorkerPool pool) {
    this.workerPool = pool;
    this.terminated = false;
    this.targetDir = DownloadTask.DOWNLOAD_TARGET_DIR_OPT_DEFAULT_VAL;
    this.tmpDir = DownloadTask.DOWNLOAD_TMP_DIR_OPT_DEFAULT_VAL;
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
    return download(url, Collections.emptyMap());
  }
  public CompletableFuture<File> download(String url, Map<String, String> options) {
    return workerPool.submit(DownloadTask.fromURL(url)
      .targetDir(targetDir)
      .tmpDir(tmpDir)
      .withOptions(options)
      .create());
  }

  public CompletableFuture<List<File>> download(List<String> urls, Map<String, String> options) {
    List<CompletableFuture<File>> files = urls.stream().map(u -> download(u, options)).collect(Collectors.toList());
    return Utils.sequence(files);
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
}
