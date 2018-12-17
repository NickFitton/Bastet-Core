package com.nfitton.imagestorage.service.impl;

import com.nfitton.imagestorage.entity.AccountType;
import com.nfitton.imagestorage.entity.User;
import com.nfitton.imagestorage.exception.InternalServerException;
import com.nfitton.imagestorage.repository.AccountRepository;
import com.nfitton.imagestorage.service.AuthenticationService;
import com.nfitton.imagestorage.service.UserService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DatabaseUserService implements UserService {

  private final AccountRepository repository;
  private final AuthenticationService authenticationService;

  @Autowired
  public DatabaseUserService(
      AccountRepository accountRepository,
      AuthenticationService authenticationService) {
    repository = accountRepository;
    this.authenticationService = authenticationService;
  }

  @Override
  public Mono<Optional<User>> findById(UUID id) {
    return Mono.fromCallable(() -> repository.findById(id));
  }

  @Override
  public Mono<User> save(User account) {
    return Mono.fromCallable(() -> repository.save(account));
  }

  @Override
  public Mono<UUID> authenticate(String email, String password, AccountType type) {
    switch (type) {
      case ADMIN:
      case BASIC:
        return authenticationService.authenticateEmail(email, password, type, repository);
      default:
        throw new InternalServerException(
            String.format("User service can't authenticate given type: %s", type));
    }
  }

  @Override
  public Flux<UUID> getAllIds() {
    return Mono.fromCallable(repository::findAllAccount).flatMapIterable(ids -> ids);
  }

  @Override
  public Mono<Boolean> deleteById(UUID id) {
    return Mono.fromCallable(() -> {
      repository.deleteById(id);
      return true;
    });
  }
}
