package com.vho.filedownload;

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


public class DownloadManagerTest {

  private static DownloadManager mgr = new DownloadManager(1);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @AfterClass
  public static void afterAll() throws Exception {
    mgr.shutdown(5, TimeUnit.SECONDS);
  }

  @Test
  public void testDownloadSuccessful() throws Exception {
    DownloadTask task  = spy(DownloadTask.class);
    final File tempFile = Files.createTempFile("", "").toFile();
    tempFile.deleteOnExit();
    when(task.call()).thenReturn(tempFile);

    assertEquals(mgr.download("http://localhost:8000/file").get(), tempFile);
  }

  @Test
  public void testDownloadThrowException() throws Exception {
    DownloadTask task  = spy(DownloadTask.class);
    when(task.call()).thenThrow(new IOException("Error while downloading file"));

    thrown.expect(ExecutionException.class);
    thrown.expectCause(isA(RuntimeException.class));
    thrown.expectMessage("Error while downloading file");

    mgr.download("http://localhost:8000/file").get();
  }

  @Test
  public void builder() {
  }
}