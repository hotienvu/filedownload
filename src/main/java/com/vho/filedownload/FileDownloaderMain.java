package com.vho.filedownload;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class FileDownloaderMain {

  private static final Logger LOG = LoggerFactory.getLogger(FileDownloaderMain.class);

  private static class Config {
    @Parameter(names = {"--url", "-u"}, required = true, description = "urls to download")
    List<String> urls = new ArrayList<>();

    @Parameter(names = {"--help", "-h"}, help = true, description = "print help")
    boolean help = false;

    @Parameter(names = {"--parallel", "-p"}, description = "number of workers to run. default is number of available CPUs")
    int numWorkers = Runtime.getRuntime().availableProcessors();

    @Parameter(names = {"--output-dir", "-o"}, required = true, description = "output dir")
    String outputDir = "";

    @Parameter(names = {"--tmp-dir", "-t"}, description = "tmp dir")
    String tmpDir = "";

    @DynamicParameter(names = {"--conf", "-c" }, description = "Configuration for each download e.g. " +
                                                                "--conf download.http.buffer.size.bytes=1024")
    Map<String, String> options = new HashMap<>();

  }

  public static void main(String[] args) {
    final Config config = new Config();
    JCommander jcmd = JCommander.newBuilder()
      .addObject(config)
      .programName("<main class> options \n" +
        "Example: <main class> \n" +
        "\t-u http://localhost:8000/pom.xml1 \n" +
        "\t-u file:///tmp/pom.xml \n" +
        "\t-o /Users/htvu/workspace/github/filedownload/target \n" +
        "\t-c download.url.scheme.file=com.vho.filedownload.downloader.local \n" +
        "\t-c download.file.overwrite=true \n" +
        "\t-c download.http.buffer.size.bytes=1024 \n" +
        "\t-p 1")
      .args(args)
      .build();

    if (config.help) {
      jcmd.usage();
      System.exit(0);
    }

    DownloadManager mgr = new DownloadManager(new WorkerPool(config.numWorkers));
    if (!config.outputDir.equals("")) mgr.setTargetDir(config.outputDir);
    if (!config.tmpDir.equals("")) mgr.setTmpDir(config.tmpDir);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      LOG.info("Trying to gracefully shutdown FileDownloader...");
      shutdownAsync(mgr);
      LOG.info("FileDownloader stopped");
    }));

    mgr.download(config.urls, config.options).whenComplete((files, e) -> {
      if (e != null) {
        e.printStackTrace();
        System.exit(1);
      }
      shutdownAsync(mgr);
    });
    mgr.awaitTermination();
  }

  private static void shutdownAsync(DownloadManager mgr) {
    // run this on another thread pool so that it won't block the shutdown of the worker pool
    CompletableFuture.runAsync(() -> mgr.shutdown(30, TimeUnit.SECONDS));
  }
}
