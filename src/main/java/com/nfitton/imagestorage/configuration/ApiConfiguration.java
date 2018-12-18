package com.nfitton.imagestorage.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfiguration {
  @Value("${api.payload.max:500}")
  private int maxPayload;

  public int getMaxPayload() {
    return maxPayload;
  }
}
