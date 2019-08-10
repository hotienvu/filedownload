package com.vho.filedownload;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FileDownloader {

  private static class Config {
    @Parameter(names = {"--url", "-u"}, description = "comma-seperated list of urls for download")
    List<String> urls = new ArrayList<>();

    @Parameter(names = {"--help", "-h"}, help = true, description = "print help")
    boolean help = false;

    @Parameter(names = {"--parallel", "-p"}, description = "number of workers to run. default is number of available CPUs")
    int numWorkers = Runtime.getRuntime().availableProcessors();

    @Parameter(names = {"--output-dir", "-o"}, description = "output dir")
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
      System.out.println("Trying to gracefully shutdown LocalDownloader...");
      mgr.shutdown(30, TimeUnit.SECONDS);
      System.out.println("LocalDownloader stopped");
    }));

    mgr.download(config.urls, config.options).whenComplete((files, e) -> {
      if (e != null) {
        e.printStackTrace();
        System.exit(1);
      }
      mgr.shutdown(30, TimeUnit.SECONDS);
    });
    mgr.awaitTermination();
  }
}
