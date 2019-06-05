package com.nfitton.imagestorage.mapper;

import static com.nfitton.imagestorage.entity.AccountType.BASIC;

import com.nfitton.imagestorage.api.UserV1;
import com.nfitton.imagestorage.entity.User;
import com.nfitton.imagestorage.util.ValidationUtil;
import java.time.ZonedDateTime;
import javax.validation.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AccountMapper {

  /**
   * Creates a new user from a given UserV1 validates it with the given validator and encodes the
   * password with the given encoder.
   *
   * @param v1        the user to convert
   * @param encoder   encoder for the password
   * @param validator validator for the UserV1 bean
   * @return a {@link User} entity to save
   */
  public static User newAccount(UserV1 v1, PasswordEncoder encoder, Validator validator) {
    ZonedDateTime now = ZonedDateTime.now();

    ValidationUtil.validate(v1, validator);

    return User.Builder
        .newBuilder()
        .withFirstName(v1.getFirstName())
        .withLastName(v1.getLastName())
        .withEmail(v1.getEmail())
        .withPassword(encoder.encode(v1.getPassword()))
        .withType(BASIC)
        .withCreatedAt(now)
        .withUpdatedAt(now)
        .withLastActive(now)
        .build();
  }

  public static UserV1 toV1(User user) {
    return new UserV1(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        null,
        user.getCreatedDate(),
        user.getLastModifiedDate(),
        user.getLastActive());
  }
}
