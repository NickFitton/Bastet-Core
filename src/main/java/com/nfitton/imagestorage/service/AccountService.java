package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.entity.AccountType;
import java.util.Optional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService<T, Id> {

  /**
   * Save the given {@link T} to a persistent storage service.
   * @param account the data to save
   * @return a record of the saved data
   */
  Mono<T> save(T account);

  /**
   * Authenticate the given id/password combination.
   * @param id the id to authenticate by
   * @param password the password attempt related to the id
   * @param type the expected type of user to be authenticating
   * @return The id of the verified account wrapped in a mono
   */
  Mono<Id> authenticate(String id, String password, AccountType type);

  Mono<Optional<T>> findById(Id id);

  Flux<Id> getAllIds();

  Mono<Boolean> existsById(Id id);

  Mono<Boolean> deleteById(Id id);
}
