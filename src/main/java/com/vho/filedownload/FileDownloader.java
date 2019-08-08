package com.vho.filedownload;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FileDownloader {

  private static class Config {
    @Parameter(names = {"--url", "-u"}, description = "comma-seperated list of urls for download")
    public List<String> urls = new ArrayList<>();

    @Parameter(names = {"--help", "-h"}, help = true, description = "print help")
    public boolean help = false;

    @Parameter(names = {"--parallel", "-p"}, description = "number of workers to run. default is number of available CPUs")
    public int numWorkers = Runtime.getRuntime().availableProcessors();
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


    DownloadManager mgr = new DownloadManager(config.numWorkers);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("Trying to gracefully shutdown FileDownloader...");
      mgr.shutdown(30, TimeUnit.SECONDS);
      System.out.println("FileDownloader stopped");
    }));

    mgr.download(config.urls).whenComplete((files, e) -> {
      if (e != null) {
        e.printStackTrace();
        System.exit(1);
      }
      mgr.shutdown(30, TimeUnit.SECONDS);
    });
    mgr.awaitTermination();
  }
}
