package com.nfitton.imagestorage.entity;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
  @OneToMany(mappedBy = "imageMetadata", cascade = CascadeType.ALL)
  private Set<ImageEntity> imageEntities;

  private ImageMetadata(
      UUID id,
      UUID cameraId,
      ZonedDateTime entryTime,
      ZonedDateTime exitTime,
      ZonedDateTime imageTime,
      ZonedDateTime createdAt,
      ZonedDateTime updatedAt,
      boolean fileExists,
      Set<ImageEntity> imageEntities) {
    this.id = id;
    this.cameraId = cameraId;
    this.entryTime = entryTime;
    this.exitTime = exitTime;
    this.imageTime = imageTime;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.fileExists = fileExists;
    this.imageEntities = imageEntities;
  }

  public ImageMetadata() {
  }

  public Set<ImageEntity> getImageEntities() {
    return imageEntities;
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
    private Collection<ImageEntity> imageEntities;

    private Builder() {
      imageEntities = new LinkedList<>();
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

    public Builder withImageEntities(Collection<ImageEntity> val) {
      imageEntities = val;
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
          fileExists,
          new HashSet<>(imageEntities));
    }
  }
}
