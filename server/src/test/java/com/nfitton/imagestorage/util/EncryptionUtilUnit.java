package com.nfitton.imagestorage.util;

import static com.google.common.io.Files.asByteSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EncryptionUtilUnit {

  private static final File testsDir = new File("build/resources/test");
  private static final File testFileA = new File("build/resources/test/a.txt");

  private static final UUID inputUuid = UUID.randomUUID();

  private UUID testId;

  @BeforeAll
  static void preconditions() throws IOException {
    if (!testsDir.exists()) {
      testsDir.mkdir();
    }
    testFileA.createNewFile();
    Files.write(testFileA.toPath(), inputUuid.toString().getBytes());
  }

  @AfterAll
  static void tearDown() {
    if (testsDir.exists() && testsDir.isDirectory()) {
      File[] files = testsDir.listFiles();
      if (files != null) {
        Arrays.stream(files)
            .filter(file -> {
              String fileName = file.getName();
              return fileName.contains(".key") || fileName.contains(".pub")
                  || fileName.contains(".enc") || fileName.contains(".ver")
                  || fileName.contains(".txt") || fileName.contains(".jpg");
            })
            .forEach(file1 -> {
              System.err.println("Removing file: " + file1.getPath());
              file1.delete();
            });
      }
    }
  }

  @BeforeEach
  void setUp() {
    testId = UUID.randomUUID();
  }

  @Test
  void aesKeysCanBeGenerated() throws NoSuchAlgorithmException {
    // GIVEN a file path
    String path = testsDir.getPath() + "/key_" + testId;

    // WHEN a key pair is generated
    EncryptionUtil.generateRsaKeys(path);

    // THEN the keys are saved to the given path
    String pvtKeyPath = path + ".key";
    String pubKeyPath = path + ".pub";
    assertTrue(Files.exists(Paths.get(pvtKeyPath)));
    assertTrue(Files.exists(Paths.get(pubKeyPath)));
  }

  @Test
  void aesPrivateKeyCanBeUsedForEncryption()
      throws IOException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException,
      BadPaddingException, NoSuchPaddingException {
    // GIVEN an aes key pair exists and a file to encrypt exists
    String path = testsDir.getPath() + "/key_" + testId;
    EncryptionUtil.generateRsaKeys(path);
    String fileLocation = testFileA.getPath();

    // WHEN a file is encrypted
    String encryptedFilePath = testsDir.getPath() + "/encrypted_" + testId;
    EncryptionUtil.doEncrypt(path + ".key", fileLocation, encryptedFilePath);

    // THEN the encrypted file exists
    encryptedFilePath = encryptedFilePath + ".enc";
    assertTrue(Files.exists(Paths.get(encryptedFilePath)));
  }

  @Test
  void encryptedFilesCanBeDecryptedWithAesPublicKey()
      throws IOException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException,
      BadPaddingException, NoSuchPaddingException {
    // GIVEN an aes key pair exists and a file to encrypt exists and has been encrypted
    String path = testsDir.getPath() + "/key_" + testId;
    EncryptionUtil.generateRsaKeys(path);
    String fileLocation = testFileA.getPath();
    String encryptedFilePath = testsDir.getPath() + "/encrypted_" + testId;
    EncryptionUtil.doEncrypt(path + ".key", fileLocation, encryptedFilePath);
    encryptedFilePath = encryptedFilePath + ".enc";

    // WHEN the file is decrypted
    String decryptedFilePath = testsDir.getPath() + "/decrypted_" + testId;
    EncryptionUtil.doDecrypt(path + ".pub", encryptedFilePath, decryptedFilePath);

    // THEN the decrypted file exists
    decryptedFilePath = decryptedFilePath + ".ver";
    assertTrue(Files.exists(Paths.get(decryptedFilePath)));

    // AND the contents of the file matches the contents of the original file
    HashCode originalFile = asByteSource(testFileA).hash(Hashing.md5());
    HashCode decryptedFile = asByteSource(new File(decryptedFilePath)).hash(Hashing.md5());
    assertEquals(originalFile, decryptedFile);
  }

  @Test
  void aesPrivateKeyCanBeUsedForEncryptingLargeFiles()
      throws IOException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException,
      BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException {
    // GIVEN an aes key pair exists and a file to encrypt exists
    URL url = Thread.currentThread().getContextClassLoader().getResource("motion/imageA.jpeg");
    File file = new File(url.getPath());
    String path = testsDir.getPath() + "/key_" + testId;
    EncryptionUtil.generateRsaKeys(path);
    String fileLocation = file.getPath();

    // WHEN a file is encrypted
    String encryptedFilePath = testsDir.getPath() + "/encrypted_" + testId;
    EncryptionUtil.encryptWithRsaAes(path + ".key", fileLocation, encryptedFilePath);

    // THEN the encrypted file exists
    encryptedFilePath = encryptedFilePath + ".enc";
    assertTrue(Files.exists(Paths.get(encryptedFilePath)));
  }

  @Test
  void largeEncryptedFilesCanBeDecryptedWithAesPublicKey()
      throws IOException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException,
      BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException {
    // GIVEN an aes key pair exists and a file to encrypt exists and has been encrypted
    URL url = Thread.currentThread().getContextClassLoader().getResource("motion/imageA.jpeg");
    File file = new File(url.getPath());
    String path = testsDir.getPath() + "/key_" + testId;
    EncryptionUtil.generateRsaKeys(path);
    String fileLocation = file.getPath();
    String encryptedFilePath = testsDir.getPath() + "/encrypted_" + testId;
    EncryptionUtil.encryptWithRsaAes(path + ".key", fileLocation, encryptedFilePath);
    encryptedFilePath = encryptedFilePath + ".enc";

    // WHEN the file is decrypted
    String decryptedFilePath = testsDir.getPath() + "/decrypted_" + testId;
    EncryptionUtil.decryptWithRsaAes(path + ".pub", encryptedFilePath, decryptedFilePath);

    // THEN the decrypted file exists
    decryptedFilePath = decryptedFilePath + ".ver";
    assertTrue(Files.exists(Paths.get(decryptedFilePath)));

    // AND the contents of the file matches the contents of the original file
    HashCode originalFile = asByteSource(file).hash(Hashing.md5());
    HashCode decryptedFile = asByteSource(new File(decryptedFilePath)).hash(Hashing.md5());
    assertEquals(originalFile, decryptedFile);
  }
}
