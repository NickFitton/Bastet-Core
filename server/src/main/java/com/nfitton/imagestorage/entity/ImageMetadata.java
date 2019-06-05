package com.nfitton.imagestorage.entity;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Entity;

@Entity
public class ImageMetadata extends BaseEntity {

  private UUID cameraId;
  private ZonedDateTime entryTime;
  private ZonedDateTime exitTime;
  private ZonedDateTime imageTime;
  private boolean fileExists;

  public ImageMetadata(
      UUID id,
      UUID cameraId,
      ZonedDateTime entryTime,
      ZonedDateTime exitTime,
      ZonedDateTime imageTime,
      ZonedDateTime createdAt,
      ZonedDateTime updatedAt,
      boolean fileExists) {
    super(id, createdAt, updatedAt);
    this.cameraId = cameraId;
    this.entryTime = entryTime;
    this.exitTime = exitTime;
    this.imageTime = imageTime;
    this.fileExists = fileExists;
  }

  public ImageMetadata() {
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

  public boolean fileExists() {
    return fileExists;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ImageMetadata)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ImageMetadata that = (ImageMetadata) o;
    return fileExists == that.fileExists
        && Objects.equals(getCameraId(), that.getCameraId())
        && Objects.equals(getEntryTime(), that.getEntryTime())
        && Objects.equals(getExitTime(), that.getExitTime())
        && Objects.equals(getImageTime(), that.getImageTime());
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(super.hashCode(), getCameraId(), getEntryTime(), getExitTime(), getImageTime(),
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

    private Builder() {
    }

    public static Builder newBuilder() {
      return new Builder();
    }

    public static Builder clone(ImageMetadata metadata) {
      return new Builder()
          .withId(metadata.getId())
          .withCameraId(metadata.getCameraId())
          .withEntryTime(metadata.entryTime)
          .withExitTime(metadata.exitTime)
          .withImageTime(metadata.imageTime)
          .withCreatedAt(metadata.getCreatedDate())
          .withUpdatedAt(metadata.getLastModifiedDate())
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
