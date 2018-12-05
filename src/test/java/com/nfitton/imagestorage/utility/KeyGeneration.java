package com.nfitton.imagestorage.utility;

import static com.nfitton.imagestorage.utility.KeyUtils.*;
import static org.junit.Assert.assertTrue;

import javax.crypto.*;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import org.junit.Test;

public class KeyGeneration {

  @Test
  public void keySharingIsSuccessful()
      throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException,
             InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException,
             IllegalBlockSizeException, IOException {
    /*
     * Alice creates her own DH key pair with 2048-bit key size
     */
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
    PublicKey alicePubKey = parseEncodedKey(encodedKeyA);

    /*
     * Bob gets the DH parameters associated with Alice's public key.
     * He must use the same parameters when he generates his own key
     * pair.
     */
    DHParameterSpec dhParamFromAlicePubKey = ((DHPublicKey) alicePubKey).getParams();

    // Bob creates his own DH key pair
    KeyPair keyPairB = keyPairFromSpec(dhParamFromAlicePubKey);

    // Bob creates and initializes his DH KeyAgreement object
    KeyAgreement bobKeyAgree = createKeyAgreement(keyPairB);

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
    bobKeyAgree.doPhase(alicePubKey, true);

    /*
     * At this stage, both Alice and Bob have completed the DH key
     * agreement protocol.
     * Both generate the (same) shared secret.
     */
    byte[] aliceSharedSecret = keyAgreementA.generateSecret();
    int aliceLen = aliceSharedSecret.length;
    byte[] bobSharedSecret = new byte[aliceLen];


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

    /*
     * Bob encrypts, using AES in CBC mode
     */
    Cipher cipherB = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipherB.init(Cipher.ENCRYPT_MODE, aesKeyB);
    byte[] cleartext = "This is just an example".getBytes();
    byte[] ciphertext = cipherB.doFinal(cleartext);

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
    byte[] recovered = aliceCipher.doFinal(ciphertext);
    assertTrue(java.util.Arrays.equals(cleartext, recovered));
  }
}
