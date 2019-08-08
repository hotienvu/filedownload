package com.vho.filedownload;


import java.io.File;
import java.util.concurrent.*;

class WorkerPool {
  private final ExecutorService pool;

  WorkerPool(int numWorkers) {
    this.pool = new ForkJoinPool(numWorkers);
  }

  CompletableFuture<File> submit(DownloadTask task) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return pool.submit(task).get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e.getMessage(), e.getCause());
      }
    });
  }

  void shutdown(long timeout, TimeUnit unit) {
    pool.shutdown();
    try {
      pool.awaitTermination(timeout, unit);
    } catch (InterruptedException e) {
      pool.shutdownNow();
    }
  }
}
