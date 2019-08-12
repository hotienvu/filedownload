package com.vho.filedownload.downloader;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class DownloadTaskTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testGetSchemeFromURL() throws Exception {
    assertEquals("hdfs", createTask("hdfs://namenode:8020/filename").getScheme());
    assertEquals("ftp", createTask("ftp://example.com/filename").getScheme());
    assertEquals("http", createTask("http://example.com/filename").getScheme());
    assertEquals("file", createTask("file:///tmp/filename").getScheme());
  }

  private DownloadTask createTask(String url) throws URISyntaxException {
    return DownloadTask.fromURL(url).tmpDir("/tmp").targetDir("/target").create();
  }

  @Test
  public void testGetCustomScheme() throws Exception {
    DownloadTask task = DownloadTask.fromURL("hdfs://namenode:8020/filename")
      .tmpDir("/tmp")
      .targetDir("/target")
      .option("download.url.scheme.hdfs", "com.downloader.hdfs")
      .create();
    assertEquals("com.downloader.hdfs", task.getScheme());
  }

  @Test
  public void testMissingTargetDir() throws Exception {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("target dir must not be null");
    DownloadTask.fromURL("http://example.com")
      .create();
  }

  @Test
  public void testMissingTmpDir() throws Exception {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("tmp dir must not be null");

    DownloadTask task = DownloadTask.fromURL("http://example.com")
      .targetDir("/target")
      .create();
  }

  @Test
  public void testCreateTaskSuccessfully() throws Exception {
    DownloadTask task = DownloadTask.fromURL("http://example.com")
      .targetDir("/target")
      .tmpDir("/tmp")
      .create();
    assertEquals(task.getUrl().toString(), "http://example.com");
    assertEquals(task.getTargetDir(), "/target");
    assertEquals(task.getTmpDir(), "/tmp");
  }
}
