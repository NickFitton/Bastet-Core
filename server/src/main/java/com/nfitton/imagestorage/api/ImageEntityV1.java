package com.nfitton.imagestorage.api;

import static javax.persistence.EnumType.STRING;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Enumerated;

public class ImageEntityV1 {

  private int x;
  private int y;
  private int width;
  private int height;
  @Enumerated(value = STRING)
  private String type;

  @JsonCreator
  public ImageEntityV1(
      @JsonProperty("x") int x,
      @JsonProperty("y") int y,
      @JsonProperty("width") int width,
      @JsonProperty("height") int height,
      @JsonProperty("type") String type) {
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

  public String getType() {
    return type;
  }
}
