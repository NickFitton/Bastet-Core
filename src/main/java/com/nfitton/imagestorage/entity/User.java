package com.nfitton.imagestorage.entity;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User extends Account {

  private String firstName;
  private String lastName;
  private String email;

  private User(
      UUID id,
      String firstName,
      String lastName,
      String email,
      String password,
      AccountType type,
      ZonedDateTime createdAt,
      ZonedDateTime updatedAt,
      ZonedDateTime lastActive) {
    super(id, password, type, createdAt, updatedAt, lastActive);
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
  }

  public User() {
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    User user = (User) o;
    return Objects.equals(firstName, user.firstName)
        && Objects.equals(lastName, user.lastName)
        && Objects.equals(email, user.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstName, lastName, email);
  }

  public static final class Builder {

    private UUID id;
    private String firstName;
    private String lastName;
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

    public Builder withFirstName(String val) {
      firstName = val.toLowerCase();
      return this;
    }

    public Builder withLastName(String val) {
      lastName = val.toLowerCase();
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
      return new User(
          id, firstName, lastName, email, password, type, createdAt, updatedAt, lastActive);
    }
  }
}
