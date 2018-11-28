package com.nfitton.imagestorage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles({"local", "h2"})
@SpringBootTest
public class ImageStorageApplicationTests {

  @Test public void contextLoads() {
  }

}
