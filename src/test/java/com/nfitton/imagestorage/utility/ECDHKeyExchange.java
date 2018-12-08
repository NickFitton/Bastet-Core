package com.nfitton.imagestorage.utility;

import javax.crypto.KeyAgreement;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;

import org.junit.Test;

public class ECDHKeyExchange {

  @Test
  public void EllipticCurveDiffieHellmanKeyExchange()
      throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
             InvalidKeyException {

    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDH", "BC");
    EllipticCurve curve = new EllipticCurve(new ECFieldFp(new BigInteger(
        "fffffffffffffffffffffffffffffffeffffffffffffffff", 16)), new BigInteger(
        "fffffffffffffffffffffffffffffffefffffffffffffffc", 16), new BigInteger(
        "fffffffffffffffffffffffffffffffefffffffffffffffc", 16));

    ECParameterSpec ecSpec = new ECParameterSpec(curve, new ECPoint(new BigInteger(
        "fffffffffffffffffffffffffffffffefffffffffffffffc", 16), new BigInteger(
        "fffffffffffffffffffffffffffffffefffffffffffffffc", 16)), new BigInteger(
        "fffffffffffffffffffffffffffffffefffffffffffffffc", 16), 1);

    keyGen.initialize(ecSpec, new SecureRandom());

    KeyAgreement aKeyAgree = KeyAgreement.getInstance("ECDH", "BC");
    KeyPair aPair = keyGen.generateKeyPair();
    KeyAgreement bKeyAgree = KeyAgreement.getInstance("ECDH", "BC");
    KeyPair bPair = keyGen.generateKeyPair();

    aKeyAgree.init(aPair.getPrivate());
    bKeyAgree.init(bPair.getPrivate());

    aKeyAgree.doPhase(bPair.getPublic(), true);
    bKeyAgree.doPhase(aPair.getPublic(), true);
    System.out.println(aKeyAgree.generateSecret().length);
    System.out.println(KeyUtils.toMinHexString(aKeyAgree.generateSecret()));
    System.out.println(KeyUtils.toMinHexString(bKeyAgree.generateSecret()));

  }
}
