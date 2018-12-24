package com.nfitton.imagestorage.exception;

public class VerificationException extends RuntimeException {

  public VerificationException() {
    super("Bad credentials/not authorized");
  }
}
