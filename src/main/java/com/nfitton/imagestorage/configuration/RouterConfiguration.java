package com.nfitton.imagestorage.configuration;

import com.nfitton.imagestorage.handler.AuthenticationHandler;
import com.nfitton.imagestorage.handler.CameraHandler;
import com.nfitton.imagestorage.handler.ImageUploadHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfiguration {

  /*
  POST
  /entity
  GET
  /entity
  GET
  /entity/count

   */

  @Bean RouterFunction<ServerResponse> routerFunction(
      ImageUploadHandler imageUploadHandler,
      AuthenticationHandler authenticationHandler,
      CameraHandler cameraHandler) {
    return nest(path("/image"),
                route(POST("/"), imageUploadHandler::uploadMetadata)
                    .andRoute(GET("/"), imageUploadHandler::getMetadataInTimeframe)
                    .andNest(
                        path("/{imageId}"),
                        route(PATCH("/"), imageUploadHandler::uploadFile)
                            .andRoute(GET("/"), imageUploadHandler::getMetadata)
                            .andRoute(GET("/image"), imageUploadHandler::getFile)))
        .andRoute(POST("/login"), authenticationHandler::login)
        .andRoute(POST("/account"), authenticationHandler::createAccount)
        .andNest(path("/camera"), route(POST("/"), cameraHandler::register)
        .andRoute(GET("/"), cameraHandler::getAll))
        .andNest(path("/entity"),
                 route(POST("/"), imageUploadHandler::uploadMetadata)
        .andRoute(GET("/"), imageUploadHandler::getMetadataInTimeframe)
        .andRoute(GET("/count"), imageUploadHandler::getMetadataCount));
  }
}
