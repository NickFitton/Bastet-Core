package com.nfitton.imagestorage.exception;

public class InternalServerException extends RuntimeException {
  public InternalServerException(Exception e) {
    super(e);
  }
}
