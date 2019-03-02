package com.nfitton.imagestorage.util;

import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.exception.VerificationException;

public class ExceptionUtil {

  public static NotFoundException userNotFound() {
    return new NotFoundException("User not found by given id");
  }

  public static NotFoundException cameraNotFound() {
    return new NotFoundException("Camera not found by given id");
  }

  public static VerificationException badCredentials() {
    return new VerificationException("Email/password combination invalid");
  }
}
