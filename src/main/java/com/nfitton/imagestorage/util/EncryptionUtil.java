package com.nfitton.imagestorage.util;

import com.nfitton.imagestorage.exception.EncryptionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Regards to jaysridhar for the tutorial on AES and RSA encryption.
 */
public class EncryptionUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionUtil.class);

  private static void processFile(Cipher ci, InputStream in, OutputStream out)
      throws IllegalBlockSizeException, BadPaddingException, IOException {
    byte[] ibuf = new byte[1024];
    int len;
    while ((len = in.read(ibuf)) != -1) {
      byte[] obuf = ci.update(ibuf, 0, len);
      if (obuf != null) {
        out.write(obuf);
      }
    }
    byte[] obuf = ci.doFinal();
    if (obuf != null) {
      out.write(obuf);
    }
  }

  private static void processFile(Cipher ci, String inFile, String outFile)
      throws IllegalBlockSizeException, BadPaddingException, IOException {
    try (FileInputStream in = new FileInputStream(inFile);
        FileOutputStream out = new FileOutputStream(outFile)) {
      processFile(ci, in, out);
    }
  }

  public static KeyPair generateRsaKeys(String fileBase) throws NoSuchAlgorithmException {
    LOGGER.debug("Creating new keypair");
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    KeyPair keyPair = kpg.generateKeyPair();
    saveKey(keyPair.getPrivate().getEncoded(), fileBase + ".key");
    saveKey(keyPair.getPublic().getEncoded(), fileBase + ".pub");
    return keyPair;
  }

  public static Optional<KeyPair> loadRsaKeys(String fileBase) {
    try {
      PrivateKey privateKey = loadPrivateKey(fileBase + ".key");
      PublicKey publicKey = loadPublicKey(fileBase + ".pub");
      KeyPair pair = new KeyPair(publicKey, privateKey);
      LOGGER.debug("Found keys and returning them");
      return Optional.of(pair);
    } catch (EncryptionException e) {
      LOGGER.error("Keys not found in given location", e);
      return Optional.empty();
    }
  }

  private static void saveKey(byte[] encodedKey, String location) {
    try (FileOutputStream out = new FileOutputStream(location)) {
      out.write(encodedKey);
    } catch (IOException e) {
      throw new EncryptionException("Failed to save given key at location: " + location, e);
    }
  }

  static void doEncrypt(String pvtKeyFile, String inputFile, String outputFile)
      throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {
    encrypt(pvtKeyFile, inputFile, outputFile);
  }

  private static void encrypt(String pvtKeyFile, String inputFile, String outputFile)
      throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException {
    PrivateKey pvt = loadPrivateKey(pvtKeyFile);
    encrypt(pvt, inputFile, outputFile);
  }

  private static void encrypt(PrivateKey pvt, String inputFile, String outputFile)
      throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException {
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, pvt);
    processFile(cipher, inputFile, outputFile + ".enc");
  }

  static void doDecrypt(String pubKeyFile, String inputFile, String outputFile)
      throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException,
      InvalidKeyException, IllegalBlockSizeException, IOException {
    decrypt(pubKeyFile, inputFile, outputFile);
  }

  private static void decrypt(String pubKeyFile, String inputFile, String outputFile)
      throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException {
    PublicKey pub = loadPublicKey(pubKeyFile);

    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.DECRYPT_MODE, pub);
    processFile(cipher, inputFile, outputFile + ".ver");
  }

  static void encryptWithRsaAes(String pvtKeyFile, String inputFile, String outputFile)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
      NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, IOException {
    encryptWithAes(pvtKeyFile, inputFile, outputFile);
  }

  public static void encryptWithRsaAes(PrivateKey pvtKeyFile, String inputFile, String outputFile)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
      NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, java.io.IOException {
    encryptWithAes(pvtKeyFile, inputFile, outputFile);
  }

  private static void encryptWithAes(String pvtKeyFile, String inputFile, String outputFile)
      throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
    PrivateKey pvt = loadPrivateKey(pvtKeyFile);
    encryptWithAes(pvt, inputFile, outputFile);
  }

  private static void encryptWithAes(PrivateKey pvt, String inputFile, String outputFile)
      throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
    SecureRandom secureRandom = new SecureRandom();

    KeyGenerator kgen = KeyGenerator.getInstance("AES");
    kgen.init(128);
    SecretKey skey = kgen.generateKey();

    byte[] iv = new byte[128 / 8];
    secureRandom.nextBytes(iv);
    IvParameterSpec ivspec = new IvParameterSpec(iv);

    try (FileOutputStream out = new FileOutputStream(outputFile + ".enc")) {
      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init(Cipher.ENCRYPT_MODE, pvt);
      byte[] b = cipher.doFinal(skey.getEncoded());
      out.write(b);
      out.write(iv);
      Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
      ci.init(Cipher.ENCRYPT_MODE, skey, ivspec);
      try (FileInputStream in = new FileInputStream(inputFile)) {
        processFile(ci, in, out);
      }
    }
  }

  static void decryptWithRsaAes(String pubKeyFile, String inputFile, String outputFile) {
    decryptWithAes(pubKeyFile, inputFile, outputFile);
  }

  public static void decryptWithRsaAes(
      PublicKey pubKeyFile, String inputFile, String outputFile) {
    decryptWithAes(pubKeyFile, inputFile, outputFile);
  }

  private static void decryptWithAes(String pubKeyFile, String inputFile, String outputFile) {
    PublicKey pub = loadPublicKey(pubKeyFile);
    decryptWithAes(pub, inputFile, outputFile);
  }

  private static void decryptWithAes(PublicKey pub, String inputFile, String outputFile) {
    try (FileInputStream in = new FileInputStream(inputFile)) {
      SecretKeySpec skey;
      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init(Cipher.DECRYPT_MODE, pub);
      byte[] b = new byte[256];
      int rsaLen = in.read(b);
      if (rsaLen == -1) {
        throw new EncryptionException("File not encrypted with RAS/AES");
      }
      byte[] keyb = cipher.doFinal(b);
      skey = new SecretKeySpec(keyb, "AES");

      byte[] iv = new byte[128 / 8];
      int aesLen = in.read(iv);
      if (aesLen == -1) {
        throw new EncryptionException("File not encrypted with RAS/AES");
      }
      IvParameterSpec ivspec = new IvParameterSpec(iv);

      Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
      ci.init(Cipher.DECRYPT_MODE, skey, ivspec);

      try (FileOutputStream out = new FileOutputStream(outputFile + ".ver")) {
        processFile(ci, in, out);
      }
    } catch (IOException | NoSuchAlgorithmException | InvalidKeyException
        | InvalidAlgorithmParameterException | NoSuchPaddingException
        | BadPaddingException | IllegalBlockSizeException e) {
      throw new EncryptionException("Error throws during decryption", e);
    }
  }

  private static PublicKey loadPublicKey(String pubKeyFile) {
    try {
      byte[] bytes = Files.readAllBytes(Paths.get(pubKeyFile));
      X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
      KeyFactory kf = KeyFactory.getInstance("RSA");
      return kf.generatePublic(ks);
    } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
      throw new EncryptionException("Failed to load public key", e);
    }
  }

  private static PrivateKey loadPrivateKey(String pvtKeyFile) {
    try {
      byte[] bytes = Files.readAllBytes(Paths.get(pvtKeyFile));
      PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
      KeyFactory kf = KeyFactory.getInstance("RSA");
      return kf.generatePrivate(ks);
    } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
      throw new EncryptionException("Failed to load private key", e);
    }
  }
}