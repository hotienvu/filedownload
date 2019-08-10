package com.vho.filedownload;

import com.vho.filedownload.task.DownloadTask;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemException;
import java.nio.file.Paths;
import java.util.Map;

import static com.vho.filedownload.Utils.checkNotNull;

public class HttpDownloader implements Downloader {
  @Override
  public File download(String url, Map<String, String> options) {
    System.out.println("Downloading " + url + " with the following settings");
    for (Map.Entry e: options.entrySet()) {
      System.out.println(e.getKey() + " = " + e.getValue());
    }
    String tmpDir = checkNotNull(options.get(DOWNLOAD_TMP_DIR_OPT_KEY));
    String targetDir = checkNotNull(options.get(DOWNLOAD_TARGET_DIR_OPT_KEY));

    URI reqURL;
    String fileName;
    try {
      reqURL = new URI(url);
      fileName = Paths.get(reqURL.getPath()).getFileName().toString();
    } catch (URISyntaxException | NullPointerException e) {
      throw new DownloadTask.FileDownloadException("Failed to parse " + url, e);
    }

    CloseableHttpClient client = HttpClients.createDefault();
    HttpUriRequest getReq = new HttpGet(reqURL);

    final File tmpFile = Paths.get(tmpDir, fileName + ".download").toFile();
    tmpFile.deleteOnExit();
    try (CloseableHttpResponse response = client.execute(getReq);
         InputStream is = response.getEntity().getContent();
         OutputStream os = new FileOutputStream(tmpFile)) {
      if (response.getStatusLine().getStatusCode() != 200)
        throw new IOException(response.getStatusLine().toString());

      String bufferSizeStr = options.getOrDefault(
        DOWNLOAD_HTTP_BUFFER_SIZE_OPT_KEY, DOWNLOAD_HTTP_BUFFER_SIZE_OPT_DEFAULT_VAL);
      int bufferSize = Integer.parseInt(bufferSizeStr);
      byte[] buffer = new byte[bufferSize];
      int totalRead = 0, read;
      while ((read = is.read(buffer, 0, bufferSize)) > 0) {
        totalRead += read;
        os.write(buffer, 0, read);
      }

      File result = Paths.get(targetDir, fileName).toFile();
      if (tmpFile.renameTo(result)) {
        System.out.println("Successfully downloaded to " + result.getAbsolutePath());
        return result;
      } else {
        throw new FileSystemException("Failed to save to " + result.getAbsolutePath());
      }
    } catch (IOException e) {
      throw new DownloadTask.FileDownloadException("Failed to download from: " + url + "Cause: " + e.getMessage(), e);
    }
  }
}
