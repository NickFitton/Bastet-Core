package com.nfitton.imagestorage.configuration;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@EnableWebFluxSecurity
class SecurityConfiguration {

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http.csrf().disable().build();
  }

  @Bean
  public PasswordEncoder getEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * CORS config allow all origins in dev.
   */
  @Bean
  public CorsWebFilter corsWebFilter(
      @Value("${idaas.endpoints.cors.allowed-origins:*}") String[] origins,
      @Value("${idaas.endpoints.cors.allowed-methods:GET,OPTIONS,POST,PATCH,PUT,DELETE}") String[] methods,
      @Value("${idaas.endpoints.cors.allowed-headers:*}") String[] headers) {
    CorsConfiguration corsConfig = new CorsConfiguration();
    corsConfig.setAllowedOrigins(Arrays.asList(origins));
    corsConfig.setAllowedMethods(Arrays.asList(methods));
    if (headers.length > 0) {
      corsConfig.setAllowedHeaders(Arrays.asList(headers));
    }

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);

    return new CorsWebFilter(source);
  }


}
