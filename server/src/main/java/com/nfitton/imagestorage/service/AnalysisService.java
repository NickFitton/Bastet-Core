package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.entity.ImageEntity;
import reactor.core.publisher.Flux;

public interface AnalysisService {

  Flux<ImageEntity> analyzeImage(String file);
}
