package com.nfitton.imagestorage.handler;

import com.nfitton.imagestorage.api.AccountV1;
import com.nfitton.imagestorage.mapper.AccountMapper;
import com.nfitton.imagestorage.service.authentication.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationHandler {

  private UserService userService;

  @Autowired public AuthenticationHandler(UserService userService) {
    this.userService = userService;
  }

  public Mono<ServerResponse> login(ServerRequest request) {
    return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  public Mono<ServerResponse> createAccount(ServerRequest request) {
    return request
        .bodyToMono(AccountV1.class)
        .map(AccountMapper::newAccount)
        .flatMap(userService::createAccount)
        .flatMap(newAccount -> ServerResponse.status(HttpStatus.CREATED).syncBody(newAccount));
  }
}
