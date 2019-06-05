package com.nfitton.imagestorage.component;

import com.nfitton.imagestorage.model.AnalysisQueueMessage;
import com.nfitton.imagestorage.model.ImageData;
import com.nfitton.imagestorage.service.FileMetadataService;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class AnalysisQueueListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisQueueListener.class);

  private final FileMetadataService fileMetadataService;

  @Autowired
  public AnalysisQueueListener(FileMetadataService fileMetadataService) {
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
    ImageData block = fileMetadataService
        .imageUploaded(message.getImageId(), Collections.emptyList()).block();

    LOGGER.debug(
        "Queue action complete, num entities found: {}",
        block.getEntities().size());

  }
}