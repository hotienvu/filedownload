package com.vho.filedownload;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class DownloadTaskTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testDifferentTaskCreation() {
    assertTrue(createTask("http://example.com/filename") instanceof DownloadTask.DownloadHttp);
    assertTrue(createTask("file:///example.com/filename") instanceof DownloadTask.DownloadLocal);
    assertTrue(createTask("ftp:///example.com/filename") instanceof DownloadTask.DownloadFtp);
  }

  @Test
  public void testUnknownURLScheme() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Unknown url scheme");
    createTask("hdfs://namenode:8020/filename");
  }

  private DownloadTask createTask(String url) {
    return DownloadTask.fromURL(url)
      .targetDir("/download")
      .create();
  }

  @Test
  public void testMissingTargetDir() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("target dir must be specified");
    DownloadTask.fromURL("http://example.com")
      .create();
  }

  @Test
  public void testSetTargetDir() {
    DownloadTask task = DownloadTask.fromURL("http://example.com")
      .targetDir("/target")
      .create();
    assertEquals(task.getTargetDir(), "/target");
  }
}