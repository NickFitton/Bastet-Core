package com.nfitton.imagestorage.component;

import com.nfitton.imagestorage.model.ImageData;
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

  /**
   * Receives a message from the queue and does analysis on the received image.
   *
   * @param message the message received from the queue
   */
  @JmsListener(destination = "analysisQueue", containerFactory = "jmsFactory")
  public void receiveMessage(AnalysisQueueMessage message) {
    LOGGER.debug("Received by queue: {}", message.getImageId());
    ImageData block = analysisService
        .analyzeImage(message.getFile())
        .collectList()
        .flatMap(entities -> fileMetadataService.imageUploaded(message.getImageId(), entities))
        .block();

    LOGGER.debug(
        "Queue action complete, num entities found: {}",
        block.getEntities().size());

  }
}