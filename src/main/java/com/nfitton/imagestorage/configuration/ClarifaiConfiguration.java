package com.nfitton.imagestorage.configuration;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClarifaiConfiguration {

  @Value("${key.clarifai}")
  private String clarifaiKey;

  private ClarifaiClient client;

  @Bean
  public ClarifaiClient getClient() {
    if (client == null) {
      client = new ClarifaiBuilder("4990fb85e85647ad9c51b339c8ae55a7").buildSync();
    }
    return client;
  }
}
