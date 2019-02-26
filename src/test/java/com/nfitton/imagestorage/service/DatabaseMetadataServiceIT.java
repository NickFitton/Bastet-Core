package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.ImageStorageApplication;
import com.nfitton.imagestorage.entity.EntityType;
import com.nfitton.imagestorage.entity.ImageEntity;
import com.nfitton.imagestorage.model.ImageData;
import com.nfitton.imagestorage.repository.FileEntityRepository;
import com.nfitton.imagestorage.repository.FileMetadataRepository;
import com.nfitton.imagestorage.service.impl.DatabaseMetadataService;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles({"local", "h2"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ImageStorageApplication.class)
public class DatabaseMetadataServiceIT {

  @Autowired
  DatabaseMetadataService metadataService;

  @Autowired
  FileEntityRepository entityRepository;

  @Autowired
  FileMetadataRepository metadataRepository;

  @Test
  public void savingMetadataIsSuccessful() {
    // GIVEN a valid Data object
    ImageData data = ImageData.Builder
        .newBuilder()
        .withCameraId(UUID.randomUUID())
        .withEntryTime(ZonedDateTime.now())
        .withExitTime(ZonedDateTime.now())
        .withImageTime(ZonedDateTime.now())
        .build();

    // WHEN data is saved to the repository
    ImageData savedData = metadataService.save(data).block();

    // THEN the returned is an updated version of the saved data
    Assertions.assertNotNull(savedData);
    Assertions.assertEquals(data.getCameraId(), savedData.getCameraId());
    Assertions.assertEquals(data.getEntryTime(), savedData.getEntryTime());
    Assertions.assertEquals(data.getExitTime(), savedData.getExitTime());
    Assertions.assertEquals(data.getImageTime(), savedData.getImageTime());
    Assertions.assertEquals(0, savedData.getEntities().size());
  }

  @Test
  public void updatingMetadataWithImageEntityIsSuccessful() {
    // GIVEN a valid Data object has been saved
    ImageData data = ImageData.Builder
        .newBuilder()
        .withCameraId(UUID.randomUUID())
        .withEntryTime(ZonedDateTime.now())
        .withExitTime(ZonedDateTime.now())
        .withImageTime(ZonedDateTime.now())
        .build();

    ImageData savedData = metadataService.save(data).block();
    Assertions.assertNotNull(savedData);
    Assertions.assertEquals(0, savedData.getEntities().size());

    // WHEN the saved data is updated with an entity
    ImageEntity entity = ImageEntity.Builder.newBuilder().withX(0)
        .withY(0)
        .withWidth(50)
        .withHeight(50)
        .withType(EntityType.OTHER)
        .build();

    data = ImageData.Builder.clone(savedData).withEntity(entity).build();
    savedData = metadataService.save(data).block();

    // THEN the data is saved and returned correctly
    Assertions.assertNotNull(savedData);
    Assertions.assertEquals(1, savedData.getEntities().size());

    // AND the data can be retrieved successfully
    savedData = metadataService.findById(savedData.getId()).block();
    Assertions.assertNotNull(savedData);
    Assertions.assertEquals(1, savedData.getEntities().size());
  }
}
