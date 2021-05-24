package io.nhartner.xrp.vanity;

import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import java.util.Map;
import java.util.function.Supplier;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.xrpl.xrpl4j.codec.addresses.Decoded;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.Version;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.codec.addresses.exceptions.DecodeException;
import org.xrpl.xrpl4j.keypairs.Ed25519KeyPairService;
import org.xrpl.xrpl4j.keypairs.HashUtils;
import org.xrpl.xrpl4j.keypairs.KeyPair;
import org.xrpl.xrpl4j.keypairs.KeyPairService;
import org.xrpl.xrpl4j.keypairs.Secp256k1KeyPairService;

/**
 * XRP Address methods copied from {@link AddressCodec} but without using the {@link
 * org.xrpl.xrpl4j.model.transactions.Address} class which causes a memory leak because it interns
 * the underlying String value.
 */
public class AddressUtils {

  private static final AddressCodec addressCodec = AddressCodec.getInstance();

  private static final Map<VersionType, Supplier<KeyPairService>> serviceMap;

  static {
    serviceMap = (new Builder<VersionType, Supplier<KeyPairService>>())
        .put(VersionType.SECP256K1, Secp256k1KeyPairService::getInstance)
        .put(VersionType.ED25519, Ed25519KeyPairService::getInstance).build();
  }

  private static KeyPairService getKeyPairServiceByType(VersionType type) {
    return (KeyPairService) ((Supplier) serviceMap.get(type)).get();
  }


  public static String computeClassicAddress(String seed) {
    KeyPair keyPair = deriveKeyPair(seed);
    UnsignedByteArray publicKeyBytes = UnsignedByteArray
        .of(BaseEncoding.base16().decode(keyPair.publicKey()));
    UnsignedByteArray addressBytes = computePublicKeyHash(publicKeyBytes);
    String address = AddressBase58
        .encode(addressBytes, Lists.newArrayList(Version.ACCOUNT_ID), UnsignedInteger.valueOf(20));
    return address;
  }

  public static UnsignedByteArray computePublicKeyHash(UnsignedByteArray publicKey) {
    byte[] sha256 = Hashing.sha256().hashBytes(publicKey.toByteArray()).asBytes();
    RIPEMD160Digest digest = new RIPEMD160Digest();
    digest.update(sha256, 0, sha256.length);
    byte[] ripemdSha256 = new byte[digest.getDigestSize()];
    digest.doFinal(ripemdSha256, 0);
    return UnsignedByteArray.of(ripemdSha256);
  }

  public static KeyPair deriveKeyPair(String seed) {
    Decoded decoded = addressCodec.decodeSeed(seed);
    if (!decoded.version().equals(Version.ED25519_SEED)) {
      throw new DecodeException(
          "Seed must use ED25519 algorithm. Algorithm was " + decoded.version());
    } else {
      return deriveKeyPair(decoded.bytes());
    }
  }

  private static KeyPair deriveKeyPair(UnsignedByteArray seed) {
    UnsignedByteArray rawPrivateKey = HashUtils.sha512Half(seed);
    Ed25519PrivateKeyParameters privateKey = new Ed25519PrivateKeyParameters(
        rawPrivateKey.toByteArray(), 0);
    Ed25519PublicKeyParameters publicKey = privateKey.generatePublicKey();
    UnsignedByte prefix = UnsignedByte.of(237);
    UnsignedByteArray prefixedPrivateKey = UnsignedByteArray.of(prefix, new UnsignedByte[0])
        .append(UnsignedByteArray.of(privateKey.getEncoded()));
    UnsignedByteArray prefixedPublicKey = UnsignedByteArray.of(prefix, new UnsignedByte[0])
        .append(UnsignedByteArray.of(publicKey.getEncoded()));
    return KeyPair.builder().privateKey(prefixedPrivateKey.hexValue())
        .publicKey(prefixedPublicKey.hexValue()).build();
  }

}
