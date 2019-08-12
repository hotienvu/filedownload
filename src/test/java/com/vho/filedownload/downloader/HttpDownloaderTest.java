package com.vho.filedownload.downloader;

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
import java.net.URISyntaxException;
import java.nio.file.FileSystemException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertArrayEquals;

public class HttpDownloaderTest {

  private static HttpServer httpServer;
  private static byte[] response = "Hello, world".getBytes();
  private static Downloader downloader = new HttpDownloader();

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

  private File download(String url, String targetDir, String tmpDir) throws DownloadTask.FileDownloadException, URISyntaxException {
    DownloadTask task = DownloadTask.fromURL(url)
      .targetDir(targetDir)
      .tmpDir(tmpDir)
      .create();

      return downloader.download(task);
  }

  @Test
  public void testDownloadSuccess() throws Exception {
    File output = download("http://localhost:8000/file", "./target", "./target");
    byte[] expected = new byte[response.length];
    new FileInputStream(output).read(expected, 0, response.length);
    assertArrayEquals(response, expected);
  }

  @Test
  public void testDownloadLinkNotFound() throws Exception {
    thrown.expect(DownloadTask.FileDownloadException.class);
    thrown.expectCause(isA(IOException.class));
    thrown.expectMessage(containsString("HTTP/1.1 404 Not Found"));
    download("http://localhost:8000/notfound", "./target", "./target");
  }

  @Test
  public void testDownloadLinkNotAFile() throws Exception {
    thrown.expect(DownloadTask.FileDownloadException.class);
    thrown.expectCause(isA(NullPointerException.class));
    thrown.expectMessage(containsString("Failed to parse"));
    download("http://localhost:8000/", "./target", "./target");
  }

  @Test
  public void testDownloadUnableToSave() throws Exception {
    thrown.expect(DownloadTask.FileDownloadException.class);
    thrown.expectCause(isA(FileSystemException.class));
    thrown.expectMessage(containsString("Failed to save to"));
    download("http://localhost:8000/file", "./foo", "./target");
  }
}
