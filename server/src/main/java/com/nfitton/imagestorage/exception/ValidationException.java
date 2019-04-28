package com.nfitton.imagestorage.exception;

import java.util.Collection;

public class ValidationException extends RuntimeException {

  private final Collection<String> violations;

  public ValidationException(Collection<String> violations) {
    super();
    this.violations = violations;
  }

  public Collection<String> getViolations() {
    return violations;
  }
}
