package com.nfitton.imagestorage.entity;

import static java.time.ZonedDateTime.now;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
public class Camera {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;
  private String password;
  private ZonedDateTime createdAt;
  private ZonedDateTime updatedAt;
  private ZonedDateTime lastUpload;

  public Camera(
      UUID id,
      String password,
      ZonedDateTime createdAt,
      ZonedDateTime updatedAt,
      ZonedDateTime lastUpload) {
    this.id = id;
    this.password = password;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.lastUpload = lastUpload;
  }

  private Camera() {
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Camera camera = (Camera) o;
    return Objects.equals(id, camera.id) && Objects.equals(password, camera.password) &&
           Objects.equals(createdAt, camera.createdAt) &&
           Objects.equals(updatedAt, camera.updatedAt) &&
           Objects.equals(lastUpload, camera.lastUpload);
  }

  @Override public int hashCode() {
    return Objects.hash(id, password, createdAt, updatedAt, lastUpload);
  }

  public void imageTaken() {
    this.lastUpload = now();
  }

  public UUID getId() {
    return id;
  }

  public String getPassword() {
    return password;
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
    private String password;
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
          .withPassword(camera.getPassword())
          .withCreatedAt(camera.getCreatedAt())
          .withUpdatedAt(camera.getUpdatedAt())
          .withLastUpload(camera.getLastUpload());
    }

    public Builder withId(UUID val) {
      id = val;
      return this;
    }

    public Builder withPassword(String val) {
      password = val;
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
      return new Camera(id, password, createdAt, updatedAt, lastUpload);
    }

  }
}
