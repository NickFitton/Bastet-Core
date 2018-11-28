package com.nfitton.imagestorage.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PathConfiguration {

  @Value("${storage.location}")
  private String location;

  public String getLocation() {
    System.currentTimeMillis();
    return location;
  }
}
