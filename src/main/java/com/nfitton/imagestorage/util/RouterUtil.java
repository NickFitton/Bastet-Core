package com.nfitton.imagestorage.util;

import com.nfitton.imagestorage.exception.BadRequestException;

import java.util.UUID;

import org.springframework.web.reactive.function.server.ServerRequest;

public class RouterUtil {

  public static UUID getUUIDParameter(String name, ServerRequest request) {
    try {
      return UUID.fromString(request.pathVariable(name));
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(String.format("Given parameter %s is not a valid UUID v4", name));
    }
  }
}
