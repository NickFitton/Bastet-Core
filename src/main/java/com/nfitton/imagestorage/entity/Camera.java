package com.nfitton.imagestorage.entity;

import static com.nfitton.imagestorage.entity.AccountType.CAMERA;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Entity;

@Entity
public class Camera extends Account {

  private UUID ownedBy;

  public Camera(
      UUID id,
      UUID ownedBy,
      String password,
      ZonedDateTime createdAt,
      ZonedDateTime updatedAt,
      ZonedDateTime lastActive) {
    super(id, password, CAMERA, createdAt, updatedAt, lastActive);
    this.ownedBy = ownedBy;
  }

  private Camera() {
    super();
  }

  public UUID getOwnedBy() {
    return ownedBy;
  }

  public static final class Builder {

    private UUID id;
    private UUID ownedBy;
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
          .withOwnedBy(camera.getOwnedBy())
          .withPassword(camera.getPassword())
          .withCreatedAt(camera.getCreatedAt())
          .withUpdatedAt(camera.getUpdatedAt())
          .withLastActive(camera.getLastActive());
    }

    public Builder withId(UUID val) {
      id = val;
      return this;
    }

    public Builder withOwnedBy(UUID val) {
      ownedBy = val;
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
      return new Camera(id, ownedBy, password, createdAt, updatedAt, lastUpload);
    }

  }
}
