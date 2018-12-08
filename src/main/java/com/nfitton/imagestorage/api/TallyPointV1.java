package com.nfitton.imagestorage.api;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TallyPointV1 {
  private ZonedDateTime time;
  private long count;

  @JsonCreator public TallyPointV1(
      @JsonProperty("time") ZonedDateTime time, @JsonProperty("count") long count) {
    this.time = time;
    this.count = count;
  }

  public ZonedDateTime getTime() {
    return time;
  }

  public long getCount() {
    return count;
  }
}
