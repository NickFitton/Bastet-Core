package com.nfitton.imagestorage.configuration;

import com.nfitton.imagestorage.handler.AuthenticationHandler;
import com.nfitton.imagestorage.handler.ImageUploadHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfiguration {

  @Bean RouterFunction<ServerResponse> routerFunction(
      ImageUploadHandler imageUploadHandler, AuthenticationHandler authenticationHandler) {
    return nest(path("/image"),
                route(POST("/"), imageUploadHandler::uploadMetadata)
                    .andRoute(GET("/"), imageUploadHandler::getMetadataInTimeframe)
                    .andNest(
                        path("/{imageId}"),
                        route(PATCH("/"), imageUploadHandler::uploadFile)
                            .andRoute(GET("/"), imageUploadHandler::getMetadata)
                            .andRoute(GET("/image"), imageUploadHandler::getFile)))
        .andRoute(POST("/login"), authenticationHandler::login)
        .andRoute(POST("/account"), authenticationHandler::createAccount);
  }
}
