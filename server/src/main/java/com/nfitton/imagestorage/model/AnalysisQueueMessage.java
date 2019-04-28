package com.nfitton.imagestorage.model;

import java.util.UUID;

public class AnalysisQueueMessage {

  private String file;
  private UUID imageId;

  public AnalysisQueueMessage() {
  }

  public AnalysisQueueMessage(String file, UUID imageId) {
    this.file = file;
    this.imageId = imageId;
  }

  public String getFile() {
    return file;
  }

  public UUID getImageId() {
    return imageId;
  }
}
