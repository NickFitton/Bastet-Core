package com.nfitton.imagestorage.model;

import java.time.ZonedDateTime;

public class TallyPoint {

  private ZonedDateTime time;
  private long count;

  public TallyPoint(ZonedDateTime time, long count) {
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
