package com.nfitton.imagestorage.repository;

import com.nfitton.imagestorage.entity.DHKey;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CameraKeyRepository extends JpaRepository<DHKey, UUID> {
}
