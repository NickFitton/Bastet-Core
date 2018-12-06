package com.nfitton.imagestorage.utility;

import static com.nfitton.imagestorage.utility.KeyUtils.*;
import static org.junit.Assert.*;

import javax.crypto.*;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import org.junit.Test;

public class KeyGeneration {

  /**
   * Diffie-Hellman implementation from https://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/CryptoSpec.html#DH2Ex
   */
  @Test public void keySharingIsSuccessful()
      throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException,
             InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException,
             IllegalBlockSizeException, IOException, ShortBufferException {

    // Alice creates her own DH key pair with 2048-bit key size
    KeyPair keyPairA = generateDHKeyPair();

    // Alice creates and initializes her DH KeyAgreement object
    KeyAgreement keyAgreementA = createKeyAgreement(keyPairA);

    // Alice encodes her public key, and sends it over to Bob.
    byte[] encodedKeyA = keyPairA.getPublic().getEncoded();

    /*
     * Let's turn over to Bob. Bob has received Alice's public key
     * in encoded format.
     * He instantiates a DH public key from the encoded key material.
     */
    DHPublicKey decodedPublicKeyA = parseEncodedKey(encodedKeyA);

    /*
     * Bob gets the DH parameters associated with Alice's public key.
     * He must use the same parameters when he generates his own key
     * pair.
     */
    DHParameterSpec dhParamFromAlicePubKey = decodedPublicKeyA.getParams();

    // Bob creates his own DH key pair
    KeyPair keyPairB = keyPairFromSpec(dhParamFromAlicePubKey);

    // Bob creates and initializes his DH KeyAgreement object
    KeyAgreement keyAgreementB = createKeyAgreement(keyPairB);

    // Bob encodes his public key, and sends it over to Alice.
    byte[] encodedKeyB = keyPairB.getPublic().getEncoded();

    /*
     * Alice uses Bob's public key for the first (and only) phase
     * of her version of the DH
     * protocol.
     * Before she can do so, she has to instantiate a DH public key
     * from Bob's encoded key material.
     */
    PublicKey publicKeyB = parseEncodedKey(encodedKeyB);
    keyAgreementA.doPhase(publicKeyB, true);

    /*
     * Bob uses Alice's public key for the first (and only) phase
     * of his version of the DH
     * protocol.
     */
    keyAgreementB.doPhase(decodedPublicKeyA, true);

    /*
     * At this stage, both Alice and Bob have completed the DH key
     * agreement protocol.
     * Both generate the (same) shared secret.
     */
    byte[] aliceSharedSecret = keyAgreementA.generateSecret();
    int aliceLen = aliceSharedSecret.length;
    byte[] bobSharedSecret = new byte[aliceLen];
    keyAgreementB.generateSecret(bobSharedSecret, 0);
    assertTrue(java.util.Arrays.equals(aliceSharedSecret, bobSharedSecret));

    /*
     * Now let's create a SecretKey object using the shared secret
     * and use it for encryption. First, we generate SecretKeys for the
     * "AES" algorithm (based on the raw shared secret data) and
     * Then we use AES in CBC mode, which requires an initialization
     * vector (IV) parameter. Note that you have to use the same IV
     * for encryption and decryption: If you use a different IV for
     * decryption than you used for encryption, decryption will fail.
     *
     * If you do not specify an IV when you initialize the Cipher
     * object for encryption, the underlying implementation will generate
     * a random one, which you have to retrieve using the
     * javax.crypto.Cipher.getParameters() method, which returns an
     * instance of java.security.AlgorithmParameters. You need to transfer
     * the contents of that object (e.g., in encoded format, obtained via
     * the AlgorithmParameters.getEncoded() method) to the party who will
     * do the decryption. When initializing the Cipher for decryption,
     * the (reinstantiated) AlgorithmParameters object must be explicitly
     * passed to the Cipher.init() method.
     */
    SecretKeySpec aesKeyB = new SecretKeySpec(bobSharedSecret, 0, 16, "AES");
    SecretKeySpec aesKeyA = new SecretKeySpec(aliceSharedSecret, 0, 16, "AES");

    testEncrypting(aesKeyB, aesKeyA);
    testEncrypting(aesKeyA, aesKeyB);

  }

  private void testEncrypting(SecretKeySpec encryptor, SecretKeySpec aesKeyA)
      throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
             IllegalBlockSizeException, BadPaddingException, IOException,
             InvalidAlgorithmParameterException {
    /*
     * Bob encrypts, using AES in CBC mode
     */
    Cipher cipherB = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipherB.init(Cipher.ENCRYPT_MODE, encryptor);
    byte[] clearText = "This is just an example".getBytes();
    byte[] cipherText = cipherB.doFinal(clearText);

    // Retrieve the parameter that was used, and transfer it to Alice in
    // encoded format
    byte[] encodedParams = cipherB.getParameters().getEncoded();

    /*
     * Alice decrypts, using AES in CBC mode
     */

    // Instantiate AlgorithmParameters object from parameter encoding
    // obtained from Bob
    AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
    aesParams.init(encodedParams);
    Cipher aliceCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    aliceCipher.init(Cipher.DECRYPT_MODE, aesKeyA, aesParams);
    byte[] recovered = aliceCipher.doFinal(cipherText);
    assertTrue(java.util.Arrays.equals(clearText, recovered));
  }

  @Test public void byteConversion() {
    byte dud = 0x1a;
    byte input = 0x6f;
    byte output = hex2byte(byte2hex(input));

    assertEquals(input, output);
    assertNotEquals(dud, output);
  }

  @Test public void keyConversion() throws NoSuchAlgorithmException {
    KeyPair keyPair = generateDHKeyPair();
    byte[] input = keyPair.getPrivate().getEncoded();
    byte[] output = fromHexString(toHexString(input));
    byte[] minOutput = fromMinHexString(toMinHexString(input));

    System.out.println(toMinHexString(keyPair.getPublic().getEncoded()));

    assertEquals(input.length, output.length);
    for (int i = 0; i < input.length; i++) {
      assertEquals(input[i], output[i], minOutput[i]);
    }
  }

  @Test public void keyAgreementPersistence()
      throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException,
             InvalidKeySpecException {
    // Bob creates his own DH key pair
    KeyPair keyPairA = generateDHKeyPair();

    KeyPair keyPairB =
        keyPairFromSpec(parseEncodedKey(keyPairA.getPublic().getEncoded()).getParams());

    // Bob creates and initializes his DH KeyAgreement object
    KeyAgreement keyAgreement1 = createKeyAgreement(keyPairA);
    KeyAgreement keyAgreement2 = createKeyAgreement(keyPairA);
    keyAgreement1.doPhase(keyPairB.getPublic(), true);
    keyAgreement2.doPhase(keyPairB.getPublic(), true);
    byte[] bytes1 = keyAgreement1.generateSecret();
    byte[] bytes2 = keyAgreement2.generateSecret();

    assertEquals(bytes1.length, bytes2.length);
    for (int i = 0; i < bytes1.length; i++) {
      assertEquals(bytes1[i], bytes2[i]);
    }
  }

  @Test public void secretlength()
      throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException,
             InvalidAlgorithmParameterException {
    for (int i = 0; i < 100; i++) {
      // Alice creates her own DH key pair with 2048-bit key size
      KeyPair keyPairA = generateDHKeyPair();

      // Alice creates and initializes her DH KeyAgreement object
      KeyAgreement keyAgreementA = createKeyAgreement(keyPairA);

      // Alice encodes her public key, and sends it over to Bob.
      byte[] encodedKeyA = keyPairA.getPublic().getEncoded();

      /*
       * Let's turn over to Bob. Bob has received Alice's public key
       * in encoded format.
       * He instantiates a DH public key from the encoded key material.
       */
      DHPublicKey decodedPublicKeyA = parseEncodedKey(encodedKeyA);

      /*
       * Bob gets the DH parameters associated with Alice's public key.
       * He must use the same parameters when he generates his own key
       * pair.
       */
      DHParameterSpec dhParamFromAlicePubKey = decodedPublicKeyA.getParams();

      // Bob creates his own DH key pair
      KeyPair keyPairB = keyPairFromSpec(dhParamFromAlicePubKey);

      // Bob creates and initializes his DH KeyAgreement object
      KeyAgreement keyAgreementB = createKeyAgreement(keyPairB);

      // Bob encodes his public key, and sends it over to Alice.
      byte[] encodedKeyB = keyPairB.getPublic().getEncoded();

      /*
       * Alice uses Bob's public key for the first (and only) phase
       * of her version of the DH
       * protocol.
       * Before she can do so, she has to instantiate a DH public key
       * from Bob's encoded key material.
       */
      PublicKey publicKeyB = parseEncodedKey(encodedKeyB);
      keyAgreementA.doPhase(publicKeyB, true);

      /*
       * Bob uses Alice's public key for the first (and only) phase
       * of his version of the DH
       * protocol.
       */
      keyAgreementB.doPhase(decodedPublicKeyA, true);

      /*
       * At this stage, both Alice and Bob have completed the DH key
       * agreement protocol.
       * Both generate the (same) shared secret.
       */
      byte[] aliceSharedSecret = keyAgreementA.generateSecret();
      System.out.print(aliceSharedSecret.length + ", ");
    }
    System.out.println();
  }
}
