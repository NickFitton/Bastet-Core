package com.nfitton.imagestorage.model;

import com.nfitton.imagestorage.entity.ImageEntity;
import com.nfitton.imagestorage.entity.ImageMetadata;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ImageData {

  private UUID id;
  private UUID cameraId;
  private ZonedDateTime entryTime;
  private ZonedDateTime exitTime;
  private ZonedDateTime imageTime;
  private ZonedDateTime createdAt;
  private ZonedDateTime updatedAt;
  private boolean fileExists;
  private List<ImageEntity> entities;

  public ImageData() {
  }

  public ImageData(
      UUID id,
      UUID cameraId,
      ZonedDateTime entryTime,
      ZonedDateTime exitTime,
      ZonedDateTime imageTime,
      ZonedDateTime createdAt,
      ZonedDateTime updatedAt,
      boolean fileExists,
      List<ImageEntity> entities) {
    this.id = id;
    this.cameraId = cameraId;
    this.entryTime = entryTime;
    this.exitTime = exitTime;
    this.imageTime = imageTime;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.fileExists = fileExists;
    this.entities = entities;
  }

  public UUID getId() {
    return id;
  }

  public UUID getCameraId() {
    return cameraId;
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

  public boolean fileExists() {
    return fileExists;
  }

  public List<ImageEntity> getEntities() {
    return entities;
  }

  public ImageMetadata asMetadata() {
    return new ImageMetadata(
        id,
        cameraId,
        entryTime,
        exitTime,
        imageTime,
        createdAt,
        updatedAt,
        fileExists);
  }

  public static final class Builder {

    private UUID id;
    private UUID cameraId;
    private ZonedDateTime entryTime;
    private ZonedDateTime exitTime;
    private ZonedDateTime imageTime;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private boolean fileExists;
    private List<ImageEntity> entities;

    private Builder() {
      entities = new LinkedList<>();
    }

    public static Builder newBuilder() {
      return new Builder();
    }

    public static Builder clone(ImageData data) {
      return new Builder()
          .withId(data.getId())
          .withCameraId(data.getCameraId())
          .withEntryTime(data.getEntryTime())
          .withExitTime(data.getExitTime())
          .withImageTime(data.getImageTime())
          .withCreatedAt(data.getCreatedAt())
          .withUpdatedAt(data.getUpdatedAt())
          .withExists(data.fileExists())
          .withEntities(data.getEntities());
    }

    public static Builder clone(ImageMetadata metadata, List<ImageEntity> entities) {
      return new Builder()
          .withId(metadata.getId())
          .withCameraId(metadata.getCameraId())
          .withEntryTime(metadata.getEntryTime())
          .withExitTime(metadata.getExitTime())
          .withImageTime(metadata.getImageTime())
          .withCreatedAt(metadata.getCreatedAt())
          .withUpdatedAt(metadata.getUpdatedAt())
          .withExists(metadata.fileExists())
          .withEntities(entities);

    }

    public Builder withId(UUID var) {
      this.id = var;
      return this;
    }

    public Builder withCameraId(UUID var) {
      this.cameraId = var;
      return this;
    }

    public Builder withEntryTime(ZonedDateTime var) {
      this.entryTime = var;
      return this;
    }

    public Builder withExitTime(ZonedDateTime var) {
      this.exitTime = var;
      return this;
    }

    public Builder withImageTime(ZonedDateTime var) {
      this.imageTime = var;
      return this;
    }

    public Builder withCreatedAt(ZonedDateTime var) {
      this.createdAt = var;
      return this;
    }

    public Builder withUpdatedAt(ZonedDateTime var) {
      this.updatedAt = var;
      return this;
    }

    public Builder withExists(boolean var) {
      this.fileExists = var;
      return this;
    }

    public Builder withEntities(List<ImageEntity> var) {
      this.entities = var;
      return this;
    }

    public Builder withEntity(ImageEntity var) {
      this.entities.add(var);
      return this;
    }

    public ImageData build() {
      return new ImageData(
          id,
          cameraId,
          entryTime,
          exitTime,
          imageTime,
          createdAt,
          updatedAt,
          fileExists,
          entities);
    }
  }
}
