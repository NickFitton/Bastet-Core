package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.entity.User;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService extends AccountService<User, UUID> {
  Mono<Boolean> idIsAdmin(UUID userId);
}
