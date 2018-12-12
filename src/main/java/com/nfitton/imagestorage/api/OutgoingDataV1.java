package com.nfitton.imagestorage.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class OutgoingDataV1 {

  private final Object data;
  private final Object error;

  @JsonCreator
  public OutgoingDataV1(@JsonProperty("data") Object data, @JsonProperty("error") Object error) {
    this.data = data;
    this.error = error;
  }

  public Object getData() {
    return data;
  }

  public Object getError() {
    return error;
  }
}
