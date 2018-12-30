package com.nfitton.imagestorage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"local", "h2"})
public class ImageStorageApplicationTests {

  @Test
  public void contextLoads() {
  }

}

