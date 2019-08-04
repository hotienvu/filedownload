package com.vho;

public class Utils {
  public static <T> T checkNotNull(T obj, String msg) {
    if (obj == null) throw new NullPointerException(msg);
    return obj;
  }

  public static <T> T checkNotNull(T obj) {
    return checkNotNull(obj, "failed null precodition check");
  }
}
