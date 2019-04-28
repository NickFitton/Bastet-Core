package com.nfitton.imagestorage.entity;

import static com.nfitton.imagestorage.entity.AccountType.CAMERA;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Entity;

@Entity
public class Camera extends Account {

  private UUID ownerId;
  private String name;

  public Camera(
      UUID id,
      UUID ownerId,
      String name,
      String password,
      ZonedDateTime createdAt,
      ZonedDateTime updatedAt,
      ZonedDateTime lastActive) {
    super(id, password, CAMERA, createdAt, updatedAt, lastActive);
    this.ownerId = ownerId;
    this.name = name;
  }

  public Camera() {
    super();
  }

  public UUID getOwnerId() {
    return ownerId;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Camera camera = (Camera) o;
    return Objects.equals(ownerId, camera.ownerId)
        && Objects.equals(name, camera.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ownerId, name);
  }

  public static final class Builder {

    private UUID id;
    private UUID ownerId;
    private String name;
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
          .withOwnerId(camera.getOwnerId())
          .withPassword(camera.getPassword())
          .withCreatedAt(camera.getCreatedDate())
          .withUpdatedAt(camera.getLastModifiedDate())
          .withLastActive(camera.getLastActive());
    }

    public Builder withId(UUID val) {
      id = val;
      return this;
    }

    public Builder withOwnerId(UUID val) {
      ownerId = val;
      return this;
    }

    public Builder withName(String val) {
      name = val;
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

    public Builder withLastActive(ZonedDateTime val) {
      lastUpload = val;
      return this;
    }

    public Camera build() {
      return new Camera(id, ownerId, name, password, createdAt, updatedAt, lastUpload);
    }

  }
}
