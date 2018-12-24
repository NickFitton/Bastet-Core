package com.nfitton.imagestorage.entity;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.hibernate.annotations.GenericGenerator;

@Entity
public class ImageMetadata {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;
  private UUID cameraId;
  private ZonedDateTime entryTime;
  private ZonedDateTime exitTime;
  private ZonedDateTime imageTime;
  private ZonedDateTime createdAt;
  private ZonedDateTime updatedAt;
  private boolean fileExists;

  private ImageMetadata(
      UUID id,
      UUID cameraId,
      ZonedDateTime entryTime,
      ZonedDateTime exitTime,
      ZonedDateTime imageTime,
      ZonedDateTime createdAt,
      ZonedDateTime updatedAt,
      boolean fileExists) {
    this.id = id;
    this.cameraId = cameraId;
    this.entryTime = entryTime;
    this.exitTime = exitTime;
    this.imageTime = imageTime;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.fileExists = fileExists;
  }

  public ImageMetadata() {
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

  public static final class Builder {

    private UUID id;
    private UUID cameraId;
    private ZonedDateTime entryTime;
    private ZonedDateTime exitTime;
    private ZonedDateTime imageTime;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private boolean fileExists;

    private Builder() {
    }

    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Hard copies the given camera object into a {@link ImageMetadata.Builder}.
     * @param metadata the camera to copy
     * @return a {@link ImageMetadata.Builder} that can have its values edited
     */
    public static Builder clone(ImageMetadata metadata) {
      return new Builder()
          .withId(metadata.getId())
          .withCameraId(metadata.getCameraId())
          .withEntryTime(metadata.entryTime)
          .withExitTime(metadata.exitTime)
          .withImageTime(metadata.imageTime)
          .withCreatedAt(metadata.createdAt)
          .withUpdatedAt(metadata.updatedAt)
          .withFileExists(metadata.fileExists);
    }

    public Builder withId(UUID val) {
      id = val;
      return this;
    }

    public Builder withCameraId(UUID val) {
      cameraId = val;
      return this;
    }

    public Builder withEntryTime(ZonedDateTime val) {
      entryTime = val;
      return this;
    }

    public Builder withExitTime(ZonedDateTime val) {
      exitTime = val;
      return this;
    }

    public Builder withImageTime(ZonedDateTime val) {
      imageTime = val;
      return this;
    }

    public Builder withCreatedAt(ZonedDateTime val) {
      createdAt = val;
      return this;
    }

    public Builder withUpdatedAt(ZonedDateTime val) {
      updatedAt = val;
      return this;
    }

    public Builder withFileExists(boolean val) {
      fileExists = val;
      return this;
    }

    /**
     * Uses the values set in the builder to construct an {@link ImageMetadata}.
     * @return the constructed {@link ImageMetadata}
     */
    public ImageMetadata build() {
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
  }
}
