package com.nfitton.imagestorage.service;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface EncryptionService {

  <T> Mono<T> decryptObject(UUID cameraId, String encryptedObject, String parameters, Class<T> objectClass);

  <T> Mono<String> encryptObject(UUID cameraId, T object, Class<T> objectClass);
}
