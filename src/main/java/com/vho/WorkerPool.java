package com.vho;


import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

class WorkerPool {
  private final ExecutorService pool;
  private final AtomicLong taskCounter = new AtomicLong();
  private final Map<Long, TaskInfo> tasks;

  WorkerPool(int numWorkers) {
    this.pool = new ForkJoinPool(numWorkers);
    this.tasks = new HashMap<>();
  }

  Future<File> submit(DownloadTask task) {
    long taskId = taskCounter.incrementAndGet();
    TaskInfo taskInfo = new TaskInfo(taskId);
    tasks.put(taskId, taskInfo);
    return pool.submit(task);
  }

  TaskStatus getStatus(long taskId) {
    return tasks.get(taskId).getStatus();
  }
}
