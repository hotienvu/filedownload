package com.vho.filedownload;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class SplitableFileDownloadTask extends RecursiveTask<File> {

  private final DownloadPart[] parts;

  public SplitableFileDownloadTask(DownloadPart[] parts) {
    this.parts = parts;
  }

  @Override
  protected File compute() {
    if (parts.length > 1) {
      return joinFiles(ForkJoinTask.invokeAll(createSubTasks(parts)).stream()
        .map(ForkJoinTask::join).collect(Collectors.toList()));
    } else {
      return download(parts[0]);
    }
  }

  private File joinFiles(List<File> collect) {
    return null;
  }

  private File download(DownloadPart part) {
    return null;
  }

  private List<SplitableFileDownloadTask> createSubTasks(DownloadPart[] parts) {
    return Arrays.stream(parts)
      .map(p -> new SplitableFileDownloadTask(new DownloadPart[] { p }))
      .collect(Collectors.toList());
  }
}
