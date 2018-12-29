package com.nfitton.imagestorage.component;

import com.nfitton.imagestorage.entity.ImageMetadata;
import com.nfitton.imagestorage.service.AnalysisService;
import com.nfitton.imagestorage.service.FileMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class AnalysisQueueListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisQueueListener.class);

  private final AnalysisService analysisService;
  private final FileMetadataService fileMetadataService;

  @Autowired
  public AnalysisQueueListener(
      AnalysisService analysisService,
      FileMetadataService fileMetadataService) {
    this.analysisService = analysisService;
    this.fileMetadataService = fileMetadataService;
  }

  @JmsListener(destination = "analysisQueue", containerFactory = "jmsFactory")
  public void receiveMessage(AnalysisQueueMessage message) {
    LOGGER.info("Received by queue: {}", message.getImageId());
    ImageMetadata block = analysisService
        .analyzeImage(message.getFile())
        .collectList()
        .flatMap(entities -> fileMetadataService.imageUploaded(message.getImageId(), entities))
        .block();

    LOGGER.info(
        "Queue action complete, num entities found: {}",
        block.getImageEntities().size());

  }
}