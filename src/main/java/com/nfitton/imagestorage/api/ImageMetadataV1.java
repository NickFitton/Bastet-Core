package com.nfitton.imagestorage.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ImageMetadataV1 {

  private UUID id;
  private ZonedDateTime entryTime;
  private ZonedDateTime exitTime;
  private ZonedDateTime imageTime;
  private ZonedDateTime createdAt;
  private ZonedDateTime updatedAt;
  private boolean fileExists;
  private List<ImageEntityV1> imageEntities;

  @JsonCreator
  public ImageMetadataV1(
      @JsonProperty("id") UUID id,
      @JsonProperty("entryTime") ZonedDateTime entryTime,
      @JsonProperty("exitTime") ZonedDateTime exitTime,
      @JsonProperty("imageTime") ZonedDateTime imageTime,
      @JsonProperty("createdAt") ZonedDateTime createdAt,
      @JsonProperty("updatedAt") ZonedDateTime updatedAt,
      @JsonProperty("fileExists") boolean fileExists,
      @JsonProperty("imageEntities") List<ImageEntityV1> imageEntities) {
    this.id = id;
    this.entryTime = entryTime;
    this.exitTime = exitTime;
    this.imageTime = imageTime;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.fileExists = fileExists;
    this.imageEntities = imageEntities;
  }

  public UUID getId() {
    return id;
  }

  public ZonedDateTime getEntryTime() {
    return entryTime;
  }

  public ZonedDateTime getExitTime() {
    return exitTime;
  }

  public ZonedDateTime getImageTime() {
    return imageTime;
  }

  public ZonedDateTime getCreatedAt() {
    return createdAt;
  }

  public ZonedDateTime getUpdatedAt() {
    return updatedAt;
  }

  public boolean isFileExists() {
    return fileExists;
  }

  public List<ImageEntityV1> getImageEntities() {
    return imageEntities;
  }
}
