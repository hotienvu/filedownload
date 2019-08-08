package com.vho.filedownload;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Utils {
  public static <T> T checkNotNull(T obj, String msg) {
    if (obj == null) throw new NullPointerException(msg);
    return obj;
  }

  public static <T> T checkNotNull(T obj) {
    return checkNotNull(obj, "failed null precodition check");
  }

  public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
    CompletableFuture<Void> fs = CompletableFuture.allOf(
      futures.toArray(new CompletableFuture[0]));

    return fs.thenApply(v ->
      futures.stream().
        map(CompletableFuture::join).
        collect(Collectors.toList())
    );
  }
}
