package com.vho.filedownload;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

class WorkerPool {
  private static final Logger LOG = LoggerFactory.getLogger(WorkerPool.class);


  private final ExecutorService pool;

  // for mockito
  protected WorkerPool(){
    this(1);
  }

  WorkerPool(int numWorkers) {
    this.pool = Executors.newFixedThreadPool(numWorkers);
  }

  <T> CompletableFuture<T> submit(Callable<T> task) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return task.call();
      } catch (Exception e) {
        LOG.error("Failed to execute task ", e);
        throw new RuntimeException(e.getMessage(), e.getCause());
      }
    }, pool);
  }

  // mostly borrowed from Guava's shutdownAndAwaitTermination
  void shutdown(long timeout, TimeUnit unit) {
    pool.shutdown();
    try {
      LOG.info("Awaiting worker pool to be completely shutdown...");
      // wait for half of time for all task to finish
      if (!pool.awaitTermination(timeout/2, unit)) {
        LOG.info("Forcing worker pool shutdown...");
        // kill all running task
        pool.shutdownNow();
        // wait for half of time for task to be killed
        pool.awaitTermination(timeout/2, unit);
      }
      LOG.info("Worker pool stopped.");
    } catch (InterruptedException e) {
      // Preserve interrupt status
      Thread.currentThread().interrupt();
      // (Re-)Cancel if current thread also interrupted
      pool.shutdownNow();
    }
  }
}