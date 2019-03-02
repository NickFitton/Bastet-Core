package com.nfitton.imagestorage.entity;

import static javax.persistence.EnumType.STRING;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Enumerated;

@Entity
public class ImageEntity extends BaseEntity {

  private UUID metadataId;
  private int x;
  private int y;
  private int width;
  private int height;
  @Enumerated(value = STRING)
  private EntityType type;

  private ImageEntity(
      UUID id,
      UUID metadataId,
      int x,
      int y,
      int width,
      int height,
      EntityType type,
      ZonedDateTime createdDate,
      ZonedDateTime lastModifiedDate) {
    super(id, createdDate, lastModifiedDate);
    this.metadataId = metadataId;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.type = type;
  }

  public ImageEntity() {
  }

  public UUID getMetadataId() {
    return metadataId;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public EntityType getType() {
    return type;
  }

  public static final class Builder {

    private UUID id;
    private UUID metadataId;
    private int x;
    private int y;
    private int width;
    private int height;
    private EntityType type;
    private ZonedDateTime createdDate;
    private ZonedDateTime lastModifiedDate;

    private Builder() {
    }

    public static Builder newBuilder() {
      return new Builder();
    }

    public static Builder clone(ImageEntity entity) {
      return new Builder()
          .withId(entity.getId())
          .withMetadataId(entity.getMetadataId())
          .withX(entity.getX())
          .withY(entity.getY())
          .withWidth(entity.getWidth())
          .withHeight(entity.getHeight())
          .withType(entity.getType());
    }

    public Builder withId(UUID val) {
      id = val;
      return this;
    }

    public Builder withMetadataId(UUID val) {
      metadataId = val;
      return this;
    }

    public Builder withX(int val) {
      x = val;
      return this;
    }

    public Builder withY(int val) {
      y = val;
      return this;
    }

    public Builder withWidth(int val) {
      width = val;
      return this;
    }

    public Builder withHeight(int val) {
      height = val;
      return this;
    }

    public Builder withType(EntityType val) {
      type = val;
      return this;
    }

    public Builder withCreatedDate(ZonedDateTime val) {
      this.createdDate = val;
      return this;
    }

    public Builder withLastModifiedDate(ZonedDateTime val) {
      this.lastModifiedDate = val;
      return this;
    }

    public ImageEntity build() {
      return new ImageEntity(
          id,
          metadataId,
          x,
          y,
          width,
          height,
          type,
          createdDate,
          lastModifiedDate);
    }
  }
}
