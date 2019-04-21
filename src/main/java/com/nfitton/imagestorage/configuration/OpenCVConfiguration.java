package com.nfitton.imagestorage.configuration;

import java.io.IOException;
import java.util.stream.Stream;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Configuration
public class OpenCVConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenCVConfiguration.class);

  @Value(value = "classpath:haar/haarcascade_eye.xml")
  private Resource eye;
  @Value(value = "classpath:haar/haarcascade_eye_tree_eyeglasses.xml")
  private Resource eyeTreeEyeglasses;
  @Value(value = "classpath:haar/haarcascade_frontalface_alt.xml")
  private Resource frontalFaceAlt;
  @Value(value = "classpath:haar/haarcascade_frontalface_alt2.xml")
  private Resource frontalFaceAltTwo;
  @Value(value = "classpath:haar/haarcascade_frontalface_alt_tree.xml")
  private Resource frontalFaceAltTree;
  @Value(value = "classpath:haar/haarcascade_frontalface_default.xml")
  private Resource frontalFaceDefault;
  @Value(value = "classpath:haar/haarcascade_fullbody.xml")
  private Resource fullBody;
  @Value(value = "classpath:haar/haarcascade_lefteye_2splits.xml")
  private Resource leftEyeTwoSplits;
  @Value(value = "classpath:haar/haarcascade_lowerbody.xml")
  private Resource lowerBody;
  @Value(value = "classpath:haar/haarcascade_profileface.xml")
  private Resource profileFace;
  @Value(value = "classpath:haar/haarcascade_righteye_2splits.xml")
  private Resource rightEyeTwoSplits;
  @Value(value = "classpath:haar/haarcascade_upperbody.xml")
  private Resource upperBody;

  public Tuple2<Resource, String> getEye() {
    return Tuples.of(eye, "EYE");
  }

  public Tuple2<Resource, String> getEyeTreeEyeglasses() {
    return Tuples.of(eyeTreeEyeglasses, "EYE");
  }

  public Tuple2<Resource, String> getFrontalFaceAlt() {
    return Tuples.of(frontalFaceAlt, "FACE");
  }

  public Tuple2<Resource, String> getFrontalFaceAltTwo() {
    return Tuples.of(frontalFaceAltTwo, "FACE");
  }

  public Tuple2<Resource, String> getFrontalFaceAltTree() {
    return Tuples.of(frontalFaceAltTree, "FACE");
  }

  public Tuple2<Resource, String> getFrontalFaceDefault() {
    return Tuples.of(frontalFaceDefault, "FACE");
  }

  public Tuple2<Resource, String> getFullBody() {
    return Tuples.of(fullBody, "BODY");
  }

  public Tuple2<Resource, String> getLeftEyeTwoSplits() {
    return Tuples.of(leftEyeTwoSplits, "EYE");
  }

  public Tuple2<Resource, String> getLowerBody() {
    return Tuples.of(lowerBody, "LOWER_BODY");
  }

  public Tuple2<Resource, String> getProfileFace() {
    return Tuples.of(profileFace, "FACE");
  }

  public Tuple2<Resource, String> getRightEyeTwoSplits() {
    return Tuples.of(rightEyeTwoSplits, "EYE");
  }

  public Tuple2<Resource, String> getUpperBody() {
    return Tuples.of(upperBody, "UPPER_BODY");
  }

  /**
   * Returns a list of all the haar cascades the system can run.
   *
   * @return a Flux stream of haar cascades and the type of entity they detect
   */
  public Flux<Tuple2<CascadeClassifier, String>> getAllCascades() {
    return Flux.fromStream(Stream.of(
        getEye(),
        getEyeTreeEyeglasses(),
        getFrontalFaceAlt(),
        getFrontalFaceAltTwo(),
        getFrontalFaceAltTree(),
        getFrontalFaceDefault(),
        getFullBody(),
        getLeftEyeTwoSplits(),
        getLowerBody(),
        getProfileFace(),
        getRightEyeTwoSplits(),
        getUpperBody()
    ))
        .flatMap(tuple2 -> {
          try {
            java.lang.String absolutePath = tuple2.getT1().getFile().getAbsolutePath();
            Tuple2<CascadeClassifier, String> tuple = Tuples
                .of(new CascadeClassifier(absolutePath), tuple2.getT2());
            return Mono.just(tuple);
          } catch (IOException e) {
            LOGGER.error("Failed to load cascade from file {}", tuple2.getT1().getFilename());
            return Mono.empty();
          }

        });
  }
}
