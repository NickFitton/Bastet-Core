package com.nfitton.imagestorage.entity;

import static java.time.ZonedDateTime.now;
import static javax.persistence.EnumType.STRING;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class Account extends BaseEntity {

  private String password;
  @Enumerated(value = STRING)
  private AccountType type;
  private ZonedDateTime lastActive;

  Account(
      UUID id,
      String password,
      AccountType type,
      ZonedDateTime createdAt,
      ZonedDateTime updatedAt,
      ZonedDateTime lastActive) {
    super(id, createdAt, updatedAt);
    this.password = password;
    this.type = type;
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

  public String getPassword() {
    return password;
  }

  public AccountType getType() {
    return type;
  }

  public ZonedDateTime getLastActive() {
    return lastActive;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Account)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Account account = (Account) o;
    return Objects.equals(getPassword(), account.getPassword())
        && getType() == account.getType()
        && Objects.equals(getLastActive(), account.getLastActive());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getPassword(), getType(), getLastActive());
  }
}
