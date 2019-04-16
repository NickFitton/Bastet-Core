package com.nfitton.imagestorage.exception;

public class EncryptionException extends RuntimeException {
  public EncryptionException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
