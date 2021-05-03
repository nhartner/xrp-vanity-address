package io.nhartner.xrp.vanity;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import io.nhartner.xrp.vanity.addresses.AddressBase58;
import io.nhartner.xrp.vanity.addresses.Version;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;
import org.xrpl.xrpl4j.keypairs.KeyPair;
import org.xrpl.xrpl4j.keypairs.KeyPairService;

/**
 * XRP Address methods copied from {@link io.nhartner.xrp.vanity.addresses.AddressCodec} but without
 * using the {@link org.xrpl.xrpl4j.model.transactions.Address} class which causes a memory leak
 * because it interns the underlying String value.
 */
public class AddressUtils {

  private static final KeyPairService KEY_PAIR_SERVICE = DefaultKeyPairService.getInstance();

  public static String computeClassicAddress(String seed) {
    KeyPair keyPair = KEY_PAIR_SERVICE.deriveKeyPair(seed);
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

}
