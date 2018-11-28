package com.nfitton.imagestorage.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
public class Camera {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;
  private String name;
  private ZonedDateTime createdAt;
  private ZonedDateTime updatedAt;
  private ZonedDateTime lastUpload;

  public Camera(
      UUID id,
      String name,
      ZonedDateTime createdAt,
      ZonedDateTime updatedAt,
      ZonedDateTime lastUpload) {
    this.id = id;
    this.name = name;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.lastUpload = lastUpload;
  }

  private Camera() {}

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

  public static final class Builder {

    private UUID id;
    private String name;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime lastUpload;

    private Builder() {
    }

    public static Builder newBuilder() {
      return new Builder();
    }

    public static Builder clone(Camera camera) {
      return new Builder()
          .withId(camera.getId())
          .withName(camera.getName())
          .withCreatedAt(camera.getCreatedAt())
          .withUpdatedAt(camera.getUpdatedAt())
          .withLastUpload(camera.getLastUpload());
    }

    public Builder withId(UUID val) {
      id = val;
      return this;
    }

    public Builder withName(String val) {
      name = val;
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

    public Builder withLastUpload(ZonedDateTime val) {
      lastUpload = val;
      return this;
    }

    public Camera build() {
      return new Camera(id, name, createdAt, updatedAt, lastUpload);
    }

  }
}
