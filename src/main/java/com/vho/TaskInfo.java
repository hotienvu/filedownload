package com.vho;

import static com.vho.TaskStatus.SUBMITED;

class TaskInfo {

  private final long taskId;
  private final TaskStatus status;

  TaskInfo(long taskId) {
    this(taskId, SUBMITED);
  }

  private TaskInfo(long taskId, TaskStatus status) {
    this.taskId = taskId;
    this.status = status;
  }

  public TaskStatus getStatus() {
    return status;
  }

  public long getTaskId() {
    return taskId;
  }
}
