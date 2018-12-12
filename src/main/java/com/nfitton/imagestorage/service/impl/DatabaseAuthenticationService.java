package com.nfitton.imagestorage.service.impl;

import com.nfitton.imagestorage.entity.Authentication;
import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.repository.AuthenticationRepository;
import com.nfitton.imagestorage.service.AuthenticationService;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DatabaseAuthenticationService implements AuthenticationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseAuthenticationService.class);

  private final AuthenticationRepository repository;

  @Autowired
  public DatabaseAuthenticationService(AuthenticationRepository repository) {
    this.repository = repository;
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
    return Mono.fromCallable(() -> repository.findById(userId));
  }

  private Mono<Optional<Authentication>> findByToken(String authToken) {
    return Mono.fromCallable(() -> repository.findByRandomString(authToken));
  }

  private Mono<UUID> revokeAuthentication(Authentication auth) {
    return Mono.fromCallable(() -> {
      repository.delete(auth);
      return auth.getUserId();
    });
  }

  private Mono<String> createAuthentication(UUID userId) {
    ZonedDateTime now = ZonedDateTime.now();
    String token = new String(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()));
    Authentication newAuthentication = new Authentication(userId, token, now);

    return Mono.fromCallable(() -> repository.save(newAuthentication))
        .map(Authentication::getRandomString);
  }

  public Flux<Authentication> getAll() {
    return Flux.fromIterable(repository.findAll());
  }
}
