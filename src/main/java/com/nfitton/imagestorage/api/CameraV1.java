package com.nfitton.imagestorage.api;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CameraV1 {
  private UUID id;
  private String name;
  private ZonedDateTime createdAt;
  private ZonedDateTime updatedAt;
  private ZonedDateTime lastUpload;

  @JsonCreator public CameraV1(
      @JsonProperty("id") UUID id,
      @JsonProperty("name") String name,
      @JsonProperty("createdAt") ZonedDateTime createdAt,
      @JsonProperty("updatedAt") ZonedDateTime updatedAt,
      @JsonProperty("lastUpload") ZonedDateTime lastUpload) {
    this.id = id;
    this.name = name;
    this.createdAt = utcOrNull(createdAt);
    this.updatedAt = utcOrNull(updatedAt);
    this.lastUpload = utcOrNull(lastUpload);
  }

  /**
   * Formats the time to be UTC, workaround to Jackson giving the incorrect zone.
   * @param time the time to parse
   * @return null if time is null, else return the given time with a zone of "UTC"
   */
  private static ZonedDateTime utcOrNull(ZonedDateTime time) {
    if (time == null) {
      return time;
    } else {
      return time.withZoneSameInstant(ZoneId.of("UTC"));
    }
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CameraV1 cameraV1 = (CameraV1) o;
    return Objects.equals(id, cameraV1.id) && Objects.equals(name, cameraV1.name) &&
           Objects.equals(createdAt, cameraV1.createdAt) &&
           Objects.equals(updatedAt, cameraV1.updatedAt) &&
           Objects.equals(lastUpload, cameraV1.lastUpload);
  }

  @Override public int hashCode() {
    return Objects.hash(id, name, createdAt, updatedAt, lastUpload);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public ZonedDateTime getCreatedAt() {
    return createdAt;
  }

  public ZonedDateTime getUpdatedAt() {
    return updatedAt;
  }

  public ZonedDateTime getLastUpload() {
    return lastUpload;
  }
}
