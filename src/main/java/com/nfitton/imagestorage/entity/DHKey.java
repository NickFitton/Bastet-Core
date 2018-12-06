package com.nfitton.imagestorage.entity;

import static com.nfitton.imagestorage.utility.KeyUtils.toMinHexString;

import javax.persistence.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "key")
public class DHKey {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;
  private UUID cameraId;
  @Enumerated(EnumType.STRING)
  @Column(name = "key_type")
  private KeyType type;
  private String privateKey;
  private String publicKey;

  private DHKey(UUID id, UUID cameraId, KeyType keyType, String privateKey, String publicKey) {
    this.id = id;
    this.cameraId = cameraId;
    this.type = keyType;
    this.privateKey = privateKey;
    this.publicKey = publicKey;
  }

  public DHKey() {
  }

  public UUID getId() {
    return id;
  }

  public UUID getCameraId() {
    return cameraId;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public KeyType getType() {
    return type;
  }

  public static final class Builder {
    private UUID id;
    private UUID cameraId;
    private KeyType keyType;
    private String privateKey;
    private String publicKey;

    private Builder() {

    }

    public static Builder newBuilder() {
      return new Builder();
    }

    public Builder withId(UUID val) {
      id = val;
      return this;
    }

    public Builder withCameraId(UUID val) {
      cameraId = val;
      return this;
    }

    public Builder withKeys(KeyPair keys) {
      withPrivate(keys.getPrivate());
      withPublic(keys.getPublic());
      return this;
    }

    public Builder withPrivate(PrivateKey val) {
      return withPrivate(val.getEncoded());
    }

    public Builder withPrivate(byte[] val) {
      privateKey = toMinHexString(val);
      return this;
    }

    public Builder withKeyType(KeyType val) {
      this.keyType = val;
      return this;
    }

    public Builder withPublic(PublicKey val) {
      return withPublic(val.getEncoded());
    }

    public Builder withPublic(byte[] val) {
      return withPublic(toMinHexString(val));
    }

    public Builder withPublic(String val) {
      publicKey = val;
      return this;
    }

    public DHKey build() {
      return new DHKey(id, cameraId, keyType, privateKey, publicKey);
    }
  }
}
