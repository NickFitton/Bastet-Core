package com.nfitton.imagestorage.service.impl;

import com.nfitton.imagestorage.entity.Account;
import com.nfitton.imagestorage.entity.AccountType;
import com.nfitton.imagestorage.entity.Authentication;
import com.nfitton.imagestorage.exception.BadRequestException;
import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.repository.AccountRepository;
import com.nfitton.imagestorage.repository.AuthenticationRepository;
import com.nfitton.imagestorage.service.AuthenticationService;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class DatabaseAuthenticationService implements AuthenticationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseAuthenticationService.class);

  private final AuthenticationRepository repository;
  private final PasswordEncoder encoder;

  @Autowired
  public DatabaseAuthenticationService(
      AuthenticationRepository repository,
      PasswordEncoder encoder) {
    this.repository = repository;
    this.encoder = encoder;
  }

  @Override
  public Mono<String> createAuthToken(UUID userId) {
    return findById(userId)
        .flatMap(optionalAuthentication -> {
          Mono<UUID> mono = Mono.just(userId);

          if (optionalAuthentication.isPresent()) {
            mono = revokeAuthentication(optionalAuthentication.get());
          }

          return mono.flatMap(this::createAuthentication);
        });
  }

  @Override
  public Mono<UUID> parseAuthentication(String authToken) {
    return findByToken(authToken).flatMap(optionalAuth -> {
      if (optionalAuth.isPresent()) {
        return Mono.just(optionalAuth.get());
      } else {
        return Mono.error(new NotFoundException("Auth token does not exist"));
      }
    }).map(Authentication::getUserId);
  }

  private Mono<Optional<Authentication>> findById(UUID userId) {
    return Mono.fromCallable(() -> repository.findById(userId)).subscribeOn(Schedulers.elastic());
  }

  private Mono<Optional<Authentication>> findByToken(String authToken) {
    return Mono.fromCallable(() -> repository.findByRandomString(authToken))
        .subscribeOn(Schedulers.elastic());
  }

  private Mono<UUID> revokeAuthentication(Authentication auth) {
    return Mono.fromCallable(() -> {
      repository.delete(auth);
      return auth.getUserId();
    }).subscribeOn(Schedulers.elastic());
  }

  private Mono<String> createAuthentication(UUID userId) {
    ZonedDateTime now = ZonedDateTime.now();
    String token = new String(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()));
    LOGGER.debug("Token created: {}", token);
    Authentication newAuthentication = new Authentication(userId, token, now);

    return Mono.fromCallable(() -> repository.save(newAuthentication))
        .map(Authentication::getRandomString).subscribeOn(Schedulers.elastic());
  }

  public Flux<Authentication> getAll() {
    return Flux.fromIterable(repository.findAll()).subscribeOn(Schedulers.elastic());
  }

  public <T extends Account> Mono<UUID> authenticate(
      UUID userId,
      String password,
      AccountType requiredType,
      JpaRepository<T, UUID> externalRepository) {
    return Mono.fromCallable(() -> externalRepository.findById(userId))
        .flatMap(account -> {
          if (account.isPresent()) {
            boolean matches = encoder.matches(password, account.get().getPassword());
            if (matches && requiredType.equals(account.get().getType())) {
              return Mono.just(account.get().getId());
            }
          }
          return Mono.error(new BadRequestException("Invalid username/password combination"));
        }).subscribeOn(Schedulers.elastic());
  }

  @Override
  public Mono<UUID> authenticateEmail(
      String emailAddress,
      String password,
      AccountType requiredType,
      AccountRepository repository) {
    return Mono.fromCallable(() -> repository.findByEmail(emailAddress))
        .flatMap(account -> {
          if (account.isPresent()) {
            boolean matches = encoder.matches(password, account.get().getPassword());
            if (matches && requiredType.equals(account.get().getType())) {
              return Mono.just(account.get().getId());
            }
          }
          return Mono.error(new BadRequestException("Invalid username/password combination"));
        }).subscribeOn(Schedulers.elastic());
  }
}
