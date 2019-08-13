package com.vho.filedownload.downloader;

import com.vho.filedownload.downloader.DownloadTask.FileDownloadException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.FileSystemException;
import java.nio.file.Paths;

public class HttpDownloader implements Downloader {

  private static final Logger LOG = LoggerFactory.getLogger(HttpDownloader.class);

  public static final String DOWNLOAD_HTTP_BUFFER_SIZE_OPT_KEY = "download.http.buffer.size.bytes";
  public static final String DOWNLOAD_HTTP_BUFFER_SIZE_OPT_DEFAULT_VAL = "4096";

  private CloseableHttpClient client = HttpClients.createDefault();

  @Override
  public File download(DownloadTask task) throws FileDownloadException {
    LOG.info("Downloading {}...", task);

    String fileName;
    try {
      fileName = Paths.get(task.getUrl().getPath()).getFileName().toString();
    } catch (NullPointerException e) {
      throw new FileDownloadException("Failed to parse " + task, e);
    }


    HttpUriRequest getReq = new HttpGet(task.getUrl());

    final File tmpFile = Paths.get(task.getTmpDir(), fileName + ".download").toFile();
    final File result = Paths.get(task.getTargetDir(), fileName).toFile();
    tmpFile.deleteOnExit();
    try (CloseableHttpResponse response = client.execute(getReq);
         InputStream is = response.getEntity().getContent();
         OutputStream os = new FileOutputStream(tmpFile)) {
      if (response.getStatusLine().getStatusCode() != 200)
        throw new IOException(response.getStatusLine().toString());

      String bufferSizeStr = task.getOptions().getOrDefault(
        DOWNLOAD_HTTP_BUFFER_SIZE_OPT_KEY, DOWNLOAD_HTTP_BUFFER_SIZE_OPT_DEFAULT_VAL);
      int bufferSize = Integer.parseInt(bufferSizeStr);
      byte[] buffer = new byte[bufferSize];
      int totalRead = 0, read;
      while ((read = is.read(buffer, 0, bufferSize)) > 0) {
        totalRead += read;
        os.write(buffer, 0, read);
      }

      if (tmpFile.renameTo(result)) {
        LOG.info("Successfully downloaded to " + result.getAbsolutePath());
      } else {
        throw new FileSystemException("Failed to save to " + result.getAbsolutePath());
      }
    } catch (IOException e) {
      throw new FileDownloadException("Failed to download from: " + task + "Cause: " + e.getMessage(), e);
    } finally {
      tmpFile.delete();
    }
    return result;
  }

  @Override
  public void close() throws Exception {
    if (client != null) {
      client.close();
    }
  }
}
