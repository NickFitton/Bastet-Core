package com.nfitton.imagestorage.util;

import java.util.Random;

public class StringUtil {
  public static String randomString(int length) {
    Random r = new Random();
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < length; i++) {
      builder.append((char) (r.nextInt(26) + 97));
    }
    return builder.toString();
  }
}
