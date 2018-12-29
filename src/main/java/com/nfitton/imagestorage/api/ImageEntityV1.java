package com.nfitton.imagestorage.api;

import static javax.persistence.EnumType.STRING;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nfitton.imagestorage.entity.EntityType;
import javax.persistence.Enumerated;

public class ImageEntityV1 {
  private int x;
  private int y;
  private int width;
  private int height;

  @JsonCreator
  public ImageEntityV1(
      @JsonProperty("x") int x,
      @JsonProperty("y") int y,
      @JsonProperty("width") int width,
      @JsonProperty("height") int height,
      @JsonProperty("type") EntityType type) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.type = type;
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

  @Enumerated(value = STRING)
  private EntityType type;
}
