package com.nfitton.imagestorage.repository;

import com.nfitton.imagestorage.entity.Camera;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CameraRepository extends JpaRepository<Camera, UUID> {
}
