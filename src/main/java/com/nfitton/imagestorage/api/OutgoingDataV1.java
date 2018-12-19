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

  public static OutgoingDataV1 dataOnly(Object data) {
    return new OutgoingDataV1(data, null);
  }

  public static OutgoingDataV1 errorOnly(Object error) {
    return new OutgoingDataV1(null, error);
  }

  public Object getData() {
    return data;
  }

  public Object getError() {
    return error;
  }
}
