package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.entity.Account;
import com.nfitton.imagestorage.entity.AccountType;
import com.nfitton.imagestorage.entity.Authentication;
import com.nfitton.imagestorage.entity.User;
import com.nfitton.imagestorage.repository.AccountRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AuthenticationService {

  /**
   * Creates an authentication token based on the given userId, if the userId already has an
   * authentication token, that one is expired and a new one is created.
   *
   * @param userId the {@link UUID} to create a token for
   * @return the auth token wrapped in a {@link Mono}
   */
  Mono<String> createAuthToken(UUID userId);

  /**
   * Receives an authentication token and returns the user id saved for the token.
   *
   * @param authToken a unique token that can identify a user
   * @return The UUID of the owner of the auth token
   */
  Mono<UUID> parseAuthentication(String authToken);

  Flux<Authentication> getAll();

  <T extends Account> Mono<UUID> authenticate(
      UUID userId,
      String password,
      AccountType requiredType,
      JpaRepository<T, UUID> repository);

  Mono<UUID> authenticateEmail(
      String emailAddress,
      String password,
      AccountType requiredType,
      AccountRepository repository);
}
