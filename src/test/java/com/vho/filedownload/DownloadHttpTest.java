package com.vho.filedownload;

import com.sun.net.httpserver.HttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.file.FileSystemException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DownloadHttpTest {

  private static HttpServer httpServer;
  private static byte[] response = "Hello, world".getBytes();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @BeforeClass
  public static void beforeAll() throws IOException {
    InetSocketAddress address = new InetSocketAddress(8000);
    httpServer = HttpServer.create(address, 0);
    httpServer.createContext("/file", exchange -> {
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
      exchange.getResponseBody().write(response);
      exchange.close();
    });

    httpServer.start();
  }

  @AfterClass
  public static void afterAll() {
    httpServer.stop(0);
  }

  private DownloadTask download(String url, String targetDir) {
    return DownloadTask.fromURL(url)
      .targetDir(targetDir)
      .create();
  }

  @Test
  public void testTaskCreation() {
    final DownloadTask task = download("http://example.com/filename", "/download");
    assertEquals(task.getTargetDir(), "/download");
    assertEquals(task.getURL(), "http://example.com/filename");
  }

  @Test
  public void testDownloadSuccess() throws Exception {
    final DownloadTask task = DownloadTask.fromURL("http://localhost:8000/file")
      .targetDir("./target")
      .tmpDir("./target")
      .create();

    File output = task.call();
    byte[] expected = new byte[response.length];
    new FileInputStream(output).read(expected, 0, response.length);
    assertArrayEquals(response, expected);
  }

  @Test
  public void testDownloadLinkNotFound() throws Exception {
    final DownloadTask task = DownloadTask.fromURL("http://localhost:8000/notfound")
      .targetDir("./target")
      .tmpDir("./target")
      .create();

    thrown.expect(DownloadTask.FileDownloadException.class);
    thrown.expectCause(isA(IOException.class));
    thrown.expectMessage(containsString("HTTP/1.1 404 Not Found"));
    task.call();
  }

  @Test
  public void testDownloadLinkNotAFile() throws Exception {
    final DownloadTask task = DownloadTask.fromURL("http://localhost:8000/")
      .targetDir("./target")
      .tmpDir("./target")
      .create();

    thrown.expect(DownloadTask.FileDownloadException.class);
    thrown.expectCause(isA(NullPointerException.class));
    thrown.expectMessage(containsString("Failed to parse"));
    task.call();
  }

  @Test
  public void testDownloadUnableToSave() throws Exception {
    final DownloadTask task = DownloadTask.fromURL("http://localhost:8000/file")
      .targetDir("./foo")
      .tmpDir("./target")
      .create();

    thrown.expect(DownloadTask.FileDownloadException.class);
    thrown.expectCause(isA(FileSystemException.class));
    thrown.expectMessage(containsString("Failed to save to"));
    task.call();
  }
}
