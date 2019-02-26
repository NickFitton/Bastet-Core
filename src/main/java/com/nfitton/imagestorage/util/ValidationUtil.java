package com.nfitton.imagestorage.util;

import com.nfitton.imagestorage.exception.ValidationException;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

public class ValidationUtil {

  public static <B> void validate(B bean, Validator validator) {
    Set<ConstraintViolation<B>> violations = validator.validate(bean);
    if (violations.size() > 0) {
      Set<String> violationReasons = violations.stream().map(
          ConstraintViolation::getMessage)
          .collect(Collectors.toSet());
      throw new ValidationException(violationReasons);
    }
  }
}
