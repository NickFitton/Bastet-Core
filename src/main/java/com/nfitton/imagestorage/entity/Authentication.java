package com.nfitton.imagestorage.entity;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.hibernate.annotations.GenericGenerator;

@Entity
public class Authentication {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID userId;
  private String randomString;
  private ZonedDateTime createdAt;

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
