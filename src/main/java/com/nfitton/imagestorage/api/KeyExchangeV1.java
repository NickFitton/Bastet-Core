package com.nfitton.imagestorage.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KeyExchangeV1 {

  private String publicKey;

  @JsonCreator
  public KeyExchangeV1(
      @JsonProperty("publicKey") String publicKey) {
    this.publicKey = publicKey;
  }

  public String getPublicKey() {
    return publicKey;
  }
}
