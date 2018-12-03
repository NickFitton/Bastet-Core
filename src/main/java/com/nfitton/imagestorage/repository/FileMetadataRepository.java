package com.nfitton.imagestorage.repository;

import com.nfitton.imagestorage.entity.ImageMetadata;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<ImageMetadata, UUID> {

  List<ImageMetadata> findAllByEntryTimeAfterAndExitTimeBefore(
      ZonedDateTime timeAfterEntry, ZonedDateTime timeBeforeExit);

  long countAllByEntryTimeAfterAndExitTimeBefore(
      ZonedDateTime timeAfterEntry,
      ZonedDateTime timeBeforeExit);
}
