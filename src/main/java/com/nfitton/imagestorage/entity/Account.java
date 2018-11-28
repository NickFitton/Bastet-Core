package com.nfitton.imagestorage.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
public class Account {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;
  private String name;
  private String email;
  private String password;
  private String salt;
  private ZonedDateTime createdAt;
  private ZonedDateTime updatedAt;
  private ZonedDateTime lastActive;

  private Account(
      UUID id,
      String name,
      String email,
      String password,
      String salt,
      ZonedDateTime createdAt,
      ZonedDateTime updatedAt,
      ZonedDateTime lastActive) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.password = password;
    this.salt = salt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.lastActive = lastActive;
  }

  public Account() {
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getSalt() {
    return salt;
  }

  public ZonedDateTime getCreatedAt() {
    return createdAt;
  }

  public ZonedDateTime getUpdatedAt() {
    return updatedAt;
  }

  public ZonedDateTime getLastActive() {
    return lastActive;
  }

  public static final class Builder {

    private UUID id;
    private String name;
    private String email;
    private String password;
    private String salt;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime lastActive;

    private Builder() {
    }

    public static Builder newBuilder() {
      return new Builder();
    }

    public static Builder clone(Account account) {
      return new Builder()
          .withId(account.id)
          .withName(account.name)
          .withEmail(account.email)
          .withPassword(account.password)
          .withCreatedAt(account.createdAt)
          .withUpdatedAt(ZonedDateTime.now())
          .withLastActive(account.lastActive);
    }

    public Builder withId(UUID val) {
      id = val;
      return this;
    }

    public Builder withName(String val) {
      name = val;
      return this;
    }

    public Builder withEmail(String val) {
      email = val;
      return this;
    }

    public Builder withPassword(String val) {
      password = val;
      return this;
    }

    public Builder withSalt(String val) {
      salt = val;
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
      lastActive = val;
      return this;
    }

    public Account build() {
      return new Account(id, name, email, password, salt, createdAt, updatedAt, lastActive);
    }
  }
}
