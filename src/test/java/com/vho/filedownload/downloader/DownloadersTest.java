package com.vho.filedownload.downloader;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertTrue;

public class DownloadersTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testDefaultURLScheme() throws Exception {
    assertTrue(Downloaders.fromScheme("http") instanceof HttpDownloader);
    assertTrue(Downloaders.fromScheme("ftp") instanceof FtpDownloader);
  }

  @Test
  public void testUnknownURLScheme() throws Exception {
    thrown.expect(Downloaders.UnsupportedURLException.class);
    thrown.expectMessage("Unsupported URL scheme");
    Downloaders.fromScheme("hdfs");
  }

  @Test
  public void testCustomURLScheme() throws Exception {
    Downloaders.fromScheme("com.vho.filedownload.downloader.hdfs");
    assertTrue(true);
  }
}
