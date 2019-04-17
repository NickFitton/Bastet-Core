package com.nfitton.imagestorage.util;

import java.util.Random;

public class StringUtil {

  /**
   * Generates a random string of letters as long as the given length.
   *
   * @param length the length the returned string should be
   * @return a randomly generated string of given length
   */
  public static String randomString(int length) {
    Random r = new Random();
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < length; i++) {
      builder.append((char) (r.nextInt(26) + 97));
    }
    return builder.toString();
  }
}
