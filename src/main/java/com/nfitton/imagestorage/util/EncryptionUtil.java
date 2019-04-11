package com.nfitton.imagestorage.util;

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
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import reactor.core.publisher.Mono;

/**
 * Regards to jaysridhar for the tutorial on AES and RSA encryption.
 */
public class EncryptionUtil {

  public static void processFile(Cipher ci, InputStream in, OutputStream out)
      throws javax.crypto.IllegalBlockSizeException,
      javax.crypto.BadPaddingException,
      java.io.IOException {
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

  public static void processFile(Cipher ci, String inFile, String outFile)
      throws javax.crypto.IllegalBlockSizeException,
      javax.crypto.BadPaddingException,
      java.io.IOException {
    try (FileInputStream in = new FileInputStream(inFile);
        FileOutputStream out = new FileOutputStream(outFile)) {
      processFile(ci, in, out);
    }
  }

  public static Mono<KeyPair> generateRsaKeys(String fileBase) {
    return Mono.fromCallable(() -> {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
      kpg.initialize(2048);
      return kpg.generateKeyPair();
    }).doOnNext(keyPair -> {
      saveKey(keyPair.getPrivate().getEncoded(), fileBase + ".key");
      saveKey(keyPair.getPublic().getEncoded(), fileBase + ".pub");
    });
  }

  public static void saveKey(byte[] encodedKey, String location) {
    try (FileOutputStream out = new FileOutputStream(location)) {
      out.write(encodedKey);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void doEncrypt(String pvtKeyFile, String inputFile)
      throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, NoSuchPaddingException,
      InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    encrypt(pvtKeyFile, inputFile, inputFile);
  }

  public static void doEncrypt(String pvtKeyFile, String inputFile, String outputFile)
      throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, NoSuchPaddingException,
      InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    encrypt(pvtKeyFile, inputFile, outputFile);
  }

  private static void encrypt(String pvtKeyFile, String inputFile, String outputFile)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
      InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    PrivateKey pvt = loadPrivateKey(pvtKeyFile);

    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, pvt);
    processFile(cipher, inputFile, outputFile + ".enc");
  }

  public static void doDecrypt(String pubKeyFile, String inputFile)
      throws java.security.NoSuchAlgorithmException, java.security.spec.InvalidKeySpecException,
      javax.crypto.NoSuchPaddingException, javax.crypto.BadPaddingException,
      java.security.InvalidKeyException, javax.crypto.IllegalBlockSizeException,
      java.io.IOException {
    decrypt(pubKeyFile, inputFile, inputFile);
  }

  public static void doDecrypt(String pubKeyFile, String inputFile, String outputFile)
      throws java.security.NoSuchAlgorithmException, java.security.spec.InvalidKeySpecException,
      javax.crypto.NoSuchPaddingException, javax.crypto.BadPaddingException,
      java.security.InvalidKeyException, javax.crypto.IllegalBlockSizeException,
      java.io.IOException {
    decrypt(pubKeyFile, inputFile, outputFile);
  }

  private static void decrypt(String pubKeyFile, String inputFile, String outputFile)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
      InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    PublicKey pub = loadPublicKey(pubKeyFile);

    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.DECRYPT_MODE, pub);
    processFile(cipher, inputFile, outputFile + ".ver");
  }

  public static void doEncryptRSAWithAES(String pvtKeyFile, String inputFile)
      throws java.security.NoSuchAlgorithmException,
      java.security.InvalidAlgorithmParameterException, java.security.InvalidKeyException,
      java.security.spec.InvalidKeySpecException, javax.crypto.NoSuchPaddingException,
      javax.crypto.BadPaddingException, javax.crypto.IllegalBlockSizeException,
      java.io.IOException {
    encryptWithAes(pvtKeyFile, inputFile, inputFile);
  }

  public static void doEncryptRSAWithAES(String pvtKeyFile, String inputFile, String outputFile)
      throws java.security.NoSuchAlgorithmException,
      java.security.InvalidAlgorithmParameterException, java.security.InvalidKeyException,
      java.security.spec.InvalidKeySpecException, javax.crypto.NoSuchPaddingException,
      javax.crypto.BadPaddingException, javax.crypto.IllegalBlockSizeException,
      java.io.IOException {
    encryptWithAes(pvtKeyFile, inputFile, outputFile);
  }

  private static void encryptWithAes(String pvtKeyFile, String inputFile, String outputFile)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
      InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
      InvalidAlgorithmParameterException {
    SecureRandom sRandom = new SecureRandom();
    PrivateKey pvt = loadPrivateKey(pvtKeyFile);

    KeyGenerator kgen = KeyGenerator.getInstance("AES");
    kgen.init(128);
    SecretKey skey = kgen.generateKey();

    byte[] iv = new byte[128 / 8];
    sRandom.nextBytes(iv);
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

  public static void doDecryptRSAWithAES(String pubKeyFile, String inputFile)
      throws java.security.NoSuchAlgorithmException,
      java.security.InvalidAlgorithmParameterException,
      java.security.InvalidKeyException,
      java.security.spec.InvalidKeySpecException,
      javax.crypto.NoSuchPaddingException,
      javax.crypto.BadPaddingException,
      javax.crypto.IllegalBlockSizeException,
      java.io.IOException {
    decryptWithAes(pubKeyFile, inputFile, inputFile);
  }

  public static void doDecryptRSAWithAES(String pubKeyFile, String inputFile, String outputFile)
      throws java.security.NoSuchAlgorithmException,
      java.security.InvalidAlgorithmParameterException,
      java.security.InvalidKeyException,
      java.security.spec.InvalidKeySpecException,
      javax.crypto.NoSuchPaddingException,
      javax.crypto.BadPaddingException,
      javax.crypto.IllegalBlockSizeException,
      java.io.IOException {
    decryptWithAes(pubKeyFile, inputFile, outputFile);
  }

  private static void decryptWithAes(String pubKeyFile, String inputFile, String outputFile)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
    PublicKey pub = loadPublicKey(pubKeyFile);

    try (FileInputStream in = new FileInputStream(inputFile)) {
      SecretKeySpec skey;
      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init(Cipher.DECRYPT_MODE, pub);
      byte[] b = new byte[256];
      in.read(b);
      byte[] keyb = cipher.doFinal(b);
      skey = new SecretKeySpec(keyb, "AES");

      byte[] iv = new byte[128 / 8];
      in.read(iv);
      IvParameterSpec ivspec = new IvParameterSpec(iv);

      Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
      ci.init(Cipher.DECRYPT_MODE, skey, ivspec);

      try (FileOutputStream out = new FileOutputStream(outputFile + ".ver")) {
        processFile(ci, in, out);
      }
    }
  }

  public static PublicKey loadPublicKey(String pubKeyFile)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] bytes = Files.readAllBytes(Paths.get(pubKeyFile));
    X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePublic(ks);
  }

  public static PrivateKey loadPrivateKey(String pvtKeyFile)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] bytes = Files.readAllBytes(Paths.get(pvtKeyFile));
    PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePrivate(ks);
  }
}