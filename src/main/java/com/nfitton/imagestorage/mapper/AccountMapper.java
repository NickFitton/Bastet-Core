package com.nfitton.imagestorage.mapper;

import static com.nfitton.imagestorage.entity.AccountType.BASIC;

import com.nfitton.imagestorage.api.UserV1;
import com.nfitton.imagestorage.entity.User;
import com.nfitton.imagestorage.util.ValidationUtil;
import java.time.ZonedDateTime;
import javax.validation.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AccountMapper {

  public static User newAccount(UserV1 v1, PasswordEncoder encoder, Validator validator) {
    ZonedDateTime now = ZonedDateTime.now();

    ValidationUtil.validate(v1, validator);

    return User.Builder
        .newBuilder()
        .withName(v1.getName())
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
        user.getName(),
        user.getEmail(),
        null,
        user.getCreatedAt(),
        user.getUpdatedAt(),
        user.getLastActive());
  }
}
