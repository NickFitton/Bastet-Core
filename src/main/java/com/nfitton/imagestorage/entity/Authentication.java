package com.nfitton.imagestorage.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Authentication {

  @Id
  private UUID userId;
  private String randomString;
  private ZonedDateTime createdAt;

  /**
   * Standard constructor.
   */
  public Authentication(UUID userId, String randomString, ZonedDateTime createdAt) {
    this.userId = userId;
    this.randomString = randomString;
    this.createdAt = createdAt;
  }

  public Authentication() {
  }

  public UUID getUserId() {
    return userId;
  }

  public ZonedDateTime getCreatedAt() {
    return createdAt;
  }

  public String getRandomString() {
    return randomString;
  }
}
