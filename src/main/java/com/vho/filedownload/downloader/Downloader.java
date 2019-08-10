package com.vho.filedownload;

import java.io.File;
import java.util.Map;

public interface Downloader {

  File download(String url, Map<String, String> options);
}
