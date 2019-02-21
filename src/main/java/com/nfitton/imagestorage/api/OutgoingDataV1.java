package com.nfitton.imagestorage.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

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

  public <T> T parseData(Class<T> dataClass, ObjectMapper mapper) {
    return mapper.convertValue(data, dataClass);
  }

  public <T> List<T> parseData(TypeReference<List<T>> typeReference, ObjectMapper mapper) {
    return mapper.convertValue(data, typeReference);
  }

  public Object getData() {
    return data;
  }

  public Object getError() {
    return error;
  }
}
