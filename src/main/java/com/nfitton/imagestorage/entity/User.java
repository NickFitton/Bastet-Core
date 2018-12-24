package com.nfitton.imagestorage.entity;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User extends Account {

  private String name;
  private String email;

  private User(
      UUID id,
      String name,
      String email,
      String password,
      AccountType type,
      ZonedDateTime createdAt,
      ZonedDateTime updatedAt,
      ZonedDateTime lastActive) {
    super(id, password, type, createdAt, updatedAt, lastActive);
    this.name = name;
    this.email = email;
  }

  public User() {
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public static final class Builder {

    private UUID id;
    private String name;
    private String email;
    private String password;
    private AccountType type;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime lastActive;

    private Builder() {
    }

    public static Builder newBuilder() {
      return new Builder();
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

    public Builder withType(AccountType val) {
      type = val;
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

    public User build() {
      return new User(id, name, email, password, type, createdAt, updatedAt, lastActive);
    }
  }
}
