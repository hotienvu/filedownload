package com.vho.filedownload;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class DownloadManagerTest {

  private static DownloadManager mgr;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @AfterClass
  public static void afterAll() throws Exception {
    mgr.shutdown(5, TimeUnit.SECONDS);
  }

  @Test
  public void testDownloadSuccessful() throws Exception {
    final String url = "http://localhost:8000/file";

    final File tempFile = Files.createTempFile("", "").toFile();
    tempFile.deleteOnExit();

    WorkerPool pool  = spy(WorkerPool.class);
    when(pool.submit(any())).thenReturn(CompletableFuture.completedFuture(tempFile));

    mgr = new DownloadManager(pool);
    assertEquals(mgr.download(url, Collections.emptyMap()).get(), tempFile);
  }

  @Test
  public void testDownloadThrowException() throws Exception {
    final String url = "http://localhost:8000/file";

    WorkerPool pool  = spy(WorkerPool.class);
    when(pool.submit(any())).thenReturn(CompletableFuture.supplyAsync(() -> {
      throw new RuntimeException("Error while downloading file", new IOException("404 not found"));
    }));

    thrown.expect(ExecutionException.class);
    thrown.expectCause(isA(RuntimeException.class));
    thrown.expectMessage("Error while downloading file");

    mgr = new DownloadManager(pool);
    mgr.download(url, Collections.emptyMap()).get();
  }

  @Test
  public void builder() {
  }
}