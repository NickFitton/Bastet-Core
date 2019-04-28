package com.nfitton.imagestorage.exception;

public class OversizeException extends RuntimeException {

  public OversizeException(int limit, int actual) {
    super(String.format("Largest payload acceptable is %s entities, request resulted in %s", limit,
                        actual));
  }
}
