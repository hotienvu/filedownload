package com.vho.filedownload;


import java.util.concurrent.*;

class WorkerPool {
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
        throw new RuntimeException(e.getMessage(), e.getCause());
      }
    }, pool);
  }

  void shutdown(long timeout, TimeUnit unit) {
    pool.shutdown();
    try {
      System.out.println("Awaiting worker pool to be completely shutdown...");
      pool.awaitTermination(timeout, unit);
    } catch (InterruptedException e) {
      System.out.println("Forcing worker pool shutdown...");
      pool.shutdownNow();
    }
  }
}