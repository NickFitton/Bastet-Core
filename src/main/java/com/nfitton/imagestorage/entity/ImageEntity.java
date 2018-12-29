package com.nfitton.imagestorage.entity;

import static javax.persistence.EnumType.STRING;

import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.GenericGenerator;

@Entity
public class ImageEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "metadata_id", referencedColumnName = "id")
  private ImageMetadata imageMetadata;
  private int x;
  private int y;
  private int width;
  private int height;
  @Enumerated(value = STRING)
  private EntityType type;

  public ImageEntity() {
  }

  public ImageEntity(
      ImageMetadata metadataId,
      int x,
      int y,
      int width,
      int height,
      EntityType type) {
    this.imageMetadata = metadataId;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.type = type;
  }

  public ImageEntity(
      int x,
      int y,
      int width,
      int height,
      EntityType type) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.type = type;
  }

  public UUID getId() {
    return id;
  }

  public ImageMetadata getImageMetadata() {
    return imageMetadata;
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
}
