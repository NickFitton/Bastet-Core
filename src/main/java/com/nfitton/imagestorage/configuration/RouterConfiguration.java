package com.nfitton.imagestorage.configuration;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.nfitton.imagestorage.handler.CameraHandlerV1;
import com.nfitton.imagestorage.handler.LoginHandlerV1;
import com.nfitton.imagestorage.handler.MotionHandlerV1;
import com.nfitton.imagestorage.handler.UserHandlerV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfiguration {

  @Bean
  RouterFunction<ServerResponse> routerFunction(
      CameraHandlerV1 cameraHandlerV1,
      LoginHandlerV1 loginHandlerV1,
      MotionHandlerV1 motionHandlerV1,
      UserHandlerV1 userHandlerV1) {
    return nest(
        path("/v1"),
        nest(
            path("/cameras"), route(POST("/"), cameraHandlerV1::postCamera)
                .andRoute(GET("/"), cameraHandlerV1::getCameras)
                .andNest(path("/{cameraId}"), route(GET("/"), cameraHandlerV1::getCamera)
                    .andRoute(DELETE("/"), cameraHandlerV1::deleteCamera)))
            .andNest(
                path("/login"), route(POST("/user"), loginHandlerV1::userLogin)
                    .andRoute(POST("/camera"), loginHandlerV1::cameraLogin))
            .andNest(path("/motion"), route(POST("/"), motionHandlerV1::postMotion)
                .andRoute(GET("/"), motionHandlerV1::getMotion)
                .andNest(
                    path("/{motionId}"),
                    route(PATCH("/"), motionHandlerV1::patchMotionPicture)
                        .andRoute(GET("/"), motionHandlerV1::getMotionById)
                        .andRoute(GET("/image"), motionHandlerV1::getMotionImageById)))
            .andNest(path("/users"), route(POST("/"), userHandlerV1::createUser)
            .andRoute(GET("/"), userHandlerV1::getUsers)
            .andRoute(GET("/{userId}"), userHandlerV1::getUser)
            .andRoute(DELETE("/{userId}"), userHandlerV1::deleteUser)
            ));
  }
}
