package com.nfitton.imagestorage.utility;

import com.nfitton.imagestorage.configuration.CryptoConfiguration;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class KeyUtils {
  private static final char[] HEX_CHARS =
      {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  public static KeyPair generateDHKeyPair() {
    KeyPairGenerator generator = CryptoConfiguration.getDHKeyPairGenerator();
    generator.initialize(2048);
    return generator.generateKeyPair();
  }

  public static KeyAgreement createKeyAgreement(KeyPair keyPair) throws InvalidKeyException {
    KeyAgreement keyAgreement = CryptoConfiguration.getDHKeyAgreement();
    keyAgreement.init(keyPair.getPrivate());
    return keyAgreement;
  }

  public static DHPublicKey parseEncodedKey(byte[] encodedKey) throws InvalidKeySpecException {
    KeyFactory keyFactory = CryptoConfiguration.getDHKeyFactory();
    X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(encodedKey);
    return (DHPublicKey) keyFactory.generatePublic(x509KeySpec);
  }

  public static KeyPair keyPairFromSpec(DHParameterSpec spec)
      throws InvalidAlgorithmParameterException {
    KeyPairGenerator keyPairGenerator = CryptoConfiguration.getDHKeyPairGenerator();
    keyPairGenerator.initialize(spec);
    return keyPairGenerator.generateKeyPair();
  }

  /*
   * Converts a byte to hex digit and writes to the supplied buffer
   */
  static String byte2hex(byte b) {
    int high = ((b & 0xf0) >> 4);
    int low = (b & 0x0f);
    return String.format("%c%c", HEX_CHARS[high], HEX_CHARS[low]);
  }

  static byte hex2byte(String str) {
    return Integer.valueOf(str, 16).byteValue();
  }

  public static String toMinHexString(byte[] block) {
    StringBuilder builder = new StringBuilder();
    for (byte aBlock : block) {
      builder.append(byte2hex(aBlock));
    }
    return builder.toString();
  }

  public static byte[] fromMinHexString(String hexString) {
    int len = hexString.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) +
                            Character.digit(hexString.charAt(i + 1), 16));
    }
    return data;

  }

  /*
   * Converts a byte array to hex string
   */
  static String toHexString(byte[] block) {
    StringBuilder builder = new StringBuilder();
    int len = block.length;
    for (int i = 0; i < len; i++) {
      builder.append(byte2hex(block[i]));
      if (i < len - 1) {
        builder.append(":");
      }
    }
    return builder.toString();
  }

  static byte[] fromHexString(String hexString) {
    hexString += ":";
    int len = hexString.length();
    byte[] data = new byte[len / 3];
    for (int i = 0; i < len; i += 3) {
      data[i / 3] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) +
                            Character.digit(hexString.charAt(i + 1), 16));
    }
    return data;
  }
}
