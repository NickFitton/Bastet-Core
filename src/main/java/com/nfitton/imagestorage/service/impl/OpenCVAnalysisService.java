package com.nfitton.imagestorage.service.impl;

import com.nfitton.imagestorage.configuration.OpenCVConfiguration;
import com.nfitton.imagestorage.entity.EntityType;
import com.nfitton.imagestorage.entity.ImageEntity;
import com.nfitton.imagestorage.service.AnalysisService;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;
import org.opencv.objdetect.Objdetect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OpenCVAnalysisService implements AnalysisService {

  private OpenCVConfiguration configuration;

  @Autowired
  public OpenCVAnalysisService(OpenCVConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public Flux<ImageEntity> analyzeImage(String file) {
    Mat image = Imgcodecs.imread(file);
    Mat grayImage = new Mat();
    Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

    int height = image.rows();
    int absFaceSize = 0;
    int minSize = Math.round(height * 0.1f);
    if (minSize > 0) {
      absFaceSize = minSize;
    }

    return Flux.concat(haarAnalysis(grayImage, absFaceSize), hogAnalysis(grayImage));
  }

  private Flux<ImageEntity> haarAnalysis(Mat image, int minSize) {
    return configuration.getAllCascades().flatMap(
        tuple2 ->
            Mono.fromCallable(() -> {
              MatOfRect bodies = new MatOfRect();
              tuple2.getT1()
                  .detectMultiScale(
                      image, bodies, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE,
                      new Size(minSize, minSize), new Size());
              return bodies;
            })
                .flatMapIterable(MatOfRect::toList)
                .map(rect -> new ImageEntity(rect.x, rect.y, rect.width, rect.height,
                                             tuple2.getT2())));
  }

  private Flux<ImageEntity> hogAnalysis(Mat image) {
    HOGDescriptor hog = new HOGDescriptor();
    hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());

    MatOfRect faces = new MatOfRect();

    hog.detectMultiScale(image, faces, new MatOfDouble());

    return Flux.fromIterable(faces.toList())
        .map(rect ->
                 new ImageEntity(rect.x, rect.y, rect.width, rect.height, EntityType.OTHER));
  }
}
