package com.nfitton.imagestorage.entity;

import static java.time.ZonedDateTime.now;
import static javax.persistence.EnumType.STRING;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import org.hibernate.annotations.GenericGenerator;

@MappedSuperclass
public class Account {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;
  private String password;
  @Enumerated(value = STRING)
  private AccountType type;
  private ZonedDateTime createdAt;
  private ZonedDateTime updatedAt;
  private ZonedDateTime lastActive;

  Account(
      UUID id,
      String password,
      AccountType type,
      ZonedDateTime createdAt,
      ZonedDateTime updatedAt,
      ZonedDateTime lastActive) {
    this.id = id;
    this.password = password;
    this.type = type;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.lastActive = lastActive;
  }

  Account() {
  }

  public void updatePassword(String newPassword) {
    password = newPassword;
  }

  public void isActive() {
    this.lastActive = now();
  }

  public UUID getId() {
    return id;
  }

  public String getPassword() {
    return password;
  }

  public AccountType getType() {
    return type;
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Account that = (Account) o;
    return Objects.equals(id, that.id) && Objects.equals(password, that.password)
        && Objects.equals(createdAt, that.createdAt)
        && Objects.equals(updatedAt, that.updatedAt)
        && Objects.equals(lastActive, that.lastActive);
  }
}
