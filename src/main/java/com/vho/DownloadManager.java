package com.vho;

import java.io.File;
import java.util.concurrent.Future;

class DownloadManager {
  private final int numWorkers;
  private final String tmpDir;
  private WorkerPool workerPool;

  private DownloadManager(Builder builder) {
    this.numWorkers = builder.numWorkers;
    this.tmpDir = builder.tmpDir;
    init();
  }

  private void init() {
    workerPool = new WorkerPool(numWorkers);
  }

  public Future<File> get(String url) {
    final DownloadTask task = DownloadTask.fromURL(url)
      .targetDir("./target")
      .tmpDir(tmpDir)
      .create();
    return workerPool.submit(task);
    /**
     * DownloadTask.fromURL(url).to("")
     */
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private int numWorkers;
    private String tmpDir;

    private Builder() {
      this.numWorkers = 1;
      this.tmpDir = "/tmp/";
    }

    public Builder numWorkers(int numWorkers) {
      this.numWorkers = numWorkers;
      return this;
    }

    public Builder tmpDir(String tmpDir) {
      this.tmpDir = tmpDir;
      return this;
    }

    public DownloadManager build() {
      return new DownloadManager(this);
    }
  }
}
