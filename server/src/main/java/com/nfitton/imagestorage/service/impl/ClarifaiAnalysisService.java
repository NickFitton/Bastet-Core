package com.nfitton.imagestorage.service.impl;

import clarifai2.api.ClarifaiClient;
import clarifai2.api.request.model.PredictRequest;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.Model;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import com.nfitton.imagestorage.entity.ImageEntity;
import com.nfitton.imagestorage.entity.ImageEntity.Builder;
import com.nfitton.imagestorage.service.AnalysisService;
import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Profile("!test")
public class ClarifaiAnalysisService implements AnalysisService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClarifaiAnalysisService.class);
  private Model<Concept> generalModel;

  @Autowired
  public ClarifaiAnalysisService(ClarifaiClient clarifaiClient) {
    generalModel = clarifaiClient.getDefaultModels().generalModel();
  }


  @Override
  public Flux<ImageEntity> analyzeImage(String file) {
    LOGGER.info("Sending file [{}] to clarifai", file);

    try {
      PredictRequest<Concept> request = generalModel
              .predict()
              .withInputs(ClarifaiInput.forImage(new File(file)));

      List<ClarifaiOutput<Concept>> result = request.executeSync().get();
      return Flux.fromStream(result.stream())
              .flatMapIterable(ClarifaiOutput::data)
              .filter(prediction -> prediction.value() >= 0.9f)
              .map(prediction -> {
                LOGGER.info("Prediction {} has ensurance of {}", prediction.name(), prediction.value());
                return Builder.newBuilder().withType(prediction.name()).build();
              });
    } catch (NoSuchElementException e) {
      LOGGER.warn("Failed to retrieve data from Clarifai");
      return Flux.empty();
    }
  }
}
