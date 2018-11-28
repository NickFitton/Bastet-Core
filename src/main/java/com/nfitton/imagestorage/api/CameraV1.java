package com.nfitton.imagestorage.api;

import java.time.ZonedDateTime;
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
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.lastUpload = lastUpload;
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
