package com.nfitton.imagestorage.configuration;

import com.nfitton.imagestorage.exception.InternalServerException;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class CryptoConfiguration {

  public static Cipher getCipher() {
    try {
      return Cipher.getInstance("AES/CBC/PKCS5Padding");
    } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
      throw new InternalServerException(e);
    }
  }

  public static KeyPairGenerator getDHKeyPairGenerator() {
    try {
     return KeyPairGenerator.getInstance("DH");
    } catch (NoSuchAlgorithmException e) {
      throw new InternalServerException(e);
    }
  }

  public static KeyAgreement getDHKeyAgreement() {
    try {
      return KeyAgreement.getInstance("DH");
    } catch (NoSuchAlgorithmException e) {
      throw new InternalServerException(e);
    }
  }

  public static KeyFactory getDHKeyFactory() {
    try {
      return KeyFactory.getInstance("DH");
    } catch (NoSuchAlgorithmException e) {
      throw new InternalServerException(e);
    }
  }
}
