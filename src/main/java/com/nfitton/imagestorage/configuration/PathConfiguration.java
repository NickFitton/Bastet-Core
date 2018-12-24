package com.nfitton.imagestorage.configuration;

import com.nfitton.imagestorage.exception.StartupException;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PathConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(PathConfiguration.class);

  @Value("${storage.create:false}")
  private boolean autoCreate;

  private String location;

  public String getLocation() {
    return location;
  }

  /**
   * Receives the storage location and validates it.
   * @param location the location stored in the config
   * @throws StartupException if the given location is a file or nonexistent
   */
  @Value("${storage.location}")
  public void setLocation(String location) throws StartupException {
    File folder = new File(location);

    if (!folder.exists() && autoCreate) {
      LOGGER.info("Storage location does not exist, creating folder at given location");
      folder.mkdirs();
    } else if (!folder.exists()) {
      throw new StartupException("Storage location does not exist");
    } else if (!folder.isDirectory()) {
      throw new StartupException("Storage location is not a directory");
    }
    this.location = location;
  }
}
