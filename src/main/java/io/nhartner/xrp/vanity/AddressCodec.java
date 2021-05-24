package io.nhartner.xrp.vanity;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.xrpl.xrpl4j.codec.addresses.ClassicAddress;
import org.xrpl.xrpl4j.codec.addresses.Decoded;
import org.xrpl.xrpl4j.codec.addresses.DecodedXAddress;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.Version;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.codec.addresses.exceptions.DecodeException;
import org.xrpl.xrpl4j.codec.addresses.exceptions.EncodeException;
import org.xrpl.xrpl4j.codec.addresses.exceptions.EncodingFormatException;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XAddress;

public class AddressCodec {
  private static final AddressCodec INSTANCE = new AddressCodec();

  public AddressCodec() {
  }

  public static AddressCodec getInstance() {
    return INSTANCE;
  }

  public Decoded decodeSeed(String seed) throws EncodingFormatException {
    Objects.requireNonNull(seed);
    return AddressBase58.decode(seed, Lists.newArrayList(new VersionType[]{VersionType.ED25519, VersionType.SECP256K1}), Lists.newArrayList(new Version[]{Version.ED25519_SEED, Version.FAMILY_SEED}), Optional.of(UnsignedInteger.valueOf(16L)));
  }

  public String encodeSeed(UnsignedByteArray entropy, VersionType type) {
    Objects.requireNonNull(entropy);
    Objects.requireNonNull(type);
    if (entropy.getUnsignedBytes().size() != 16) {
      throw new EncodeException("entropy must have length 16.");
    } else {
      Version version = type.equals(VersionType.ED25519) ? Version.ED25519_SEED : Version.FAMILY_SEED;
      return AddressBase58.encode(entropy, Lists.newArrayList(new Version[]{version}), UnsignedInteger.valueOf(16L));
    }
  }

  public Address encodeAccountId(UnsignedByteArray accountId) {
    Objects.requireNonNull(accountId);
    return Address.of(AddressBase58.encode(accountId, Lists.newArrayList(new Version[]{Version.ACCOUNT_ID}), UnsignedInteger.valueOf(20L)));
  }

  public UnsignedByteArray decodeAccountId(Address accountId) {
    Objects.requireNonNull(accountId);
    return AddressBase58.decode(accountId.value(), Lists.newArrayList(new Version[]{Version.ACCOUNT_ID}), UnsignedInteger.valueOf(20L)).bytes();
  }

  public String encodeNodePublicKey(UnsignedByteArray publicKey) {
    Objects.requireNonNull(publicKey);
    return AddressBase58.encode(publicKey, Lists.newArrayList(new Version[]{Version.NODE_PUBLIC}), UnsignedInteger.valueOf(33L));
  }

  public UnsignedByteArray decodeNodePublicKey(String publicKey) {
    Objects.requireNonNull(publicKey);
    return AddressBase58.decode(publicKey, Lists.newArrayList(new Version[]{Version.NODE_PUBLIC}), UnsignedInteger.valueOf(33L)).bytes();
  }

  public String encodeAccountPublicKey(UnsignedByteArray publicKey) {
    Objects.requireNonNull(publicKey);
    return AddressBase58.encode(publicKey, Lists.newArrayList(new Version[]{Version.ACCOUNT_PUBLIC_KEY}), UnsignedInteger.valueOf(33L));
  }

  public UnsignedByteArray decodeAccountPublicKey(String publicKey) {
    Objects.requireNonNull(publicKey);
    return AddressBase58.decode(publicKey, Lists.newArrayList(new Version[]{Version.ACCOUNT_PUBLIC_KEY}), UnsignedInteger.valueOf(33L)).bytes();
  }

  public XAddress classicAddressToXAddress(Address classicAddress, UnsignedInteger tag, boolean test) {
    Objects.requireNonNull(classicAddress);
    Objects.requireNonNull(tag);
    return this.classicAddressToXAddress(classicAddress, Optional.of(tag), test);
  }

  public XAddress classicAddressToXAddress(Address classicAddress, boolean test) {
    Objects.requireNonNull(classicAddress);
    return this.classicAddressToXAddress(classicAddress, Optional.empty(), test);
  }

  public XAddress classicAddressToXAddress(Address classicAddress, Optional<UnsignedInteger> tag, boolean test) {
    Objects.requireNonNull(classicAddress);
    Objects.requireNonNull(tag);
    UnsignedByteArray accountId = this.decodeAccountId(classicAddress);
    return this.encodeXAddress(accountId, tag, test);
  }

  private XAddress encodeXAddress(UnsignedByteArray accountId, Optional<UnsignedInteger> tag, boolean test) {
    Objects.requireNonNull(accountId);
    Objects.requireNonNull(tag);
    if (accountId.getUnsignedBytes().size() != 20) {
      throw new EncodeException("AccountID must be 20 bytes.");
    } else {
      byte flag;
      UnsignedInteger normalizedTag;
      if (tag.isPresent()) {
        flag = 1;
        normalizedTag = (UnsignedInteger)tag.get();
      } else {
        flag = 0;
        normalizedTag = UnsignedInteger.ZERO;
      }

      UnsignedByteArray bytes = UnsignedByteArray.of(test ? AddressCodec.PrefixBytes.TEST : AddressCodec.PrefixBytes.MAIN).append(accountId).append(UnsignedByteArray.of(new byte[]{(byte)flag, (byte)(normalizedTag.intValue() & 255), (byte)(normalizedTag.intValue() >>> 8 & 255), (byte)(normalizedTag.intValue() >>> 16 & 255), (byte)(normalizedTag.intValue() >>> 24 & 255), 0, 0, 0, 0}));
      return XAddress.of(Base58.encodeChecked(bytes.toByteArray()));
    }
  }

  public ClassicAddress xAddressToClassicAddress(XAddress xAddress) {
    Objects.requireNonNull(xAddress);
    DecodedXAddress decodedXAddress = this.decodeXAddress(xAddress);
    Address classicAddress = this.encodeAccountId(decodedXAddress.accountId());
    return ClassicAddress.builder().classicAddress(classicAddress).tag(decodedXAddress.tag()).test(decodedXAddress.test()).build();
  }

  private DecodedXAddress decodeXAddress(XAddress xAddress) {
    Objects.requireNonNull(xAddress);
    byte[] decoded = Base58.decodeChecked(xAddress.value());
    boolean test = this.isTestAddress(decoded);
    byte[] accountId = Arrays.copyOfRange(decoded, 2, 22);
    UnsignedInteger tag = this.tagFromDecodedXAddress(decoded);
    return DecodedXAddress.builder().accountId(UnsignedByteArray.of(accountId)).tag(tag).test(test).build();
  }

  private UnsignedInteger tagFromDecodedXAddress(byte[] decoded) {
    Objects.requireNonNull(decoded);
    byte flag = decoded[22];
    if (flag >= 2) {
      throw new DecodeException("Unsupported X-Address: 64-bit tags are not supported");
    } else if (flag == 1) {
      return UnsignedInteger.valueOf((long)(decoded[23] & 255)).plus(UnsignedInteger.valueOf((long)((decoded[24] & 255) * 256))).plus(UnsignedInteger.valueOf((long)((decoded[25] & 255) * 65536))).plus(UnsignedInteger.valueOf(16777216L).times(UnsignedInteger.valueOf((long)(decoded[26] & 255))));
    } else if (flag == 0) {
      byte[] endBytes = new byte[8];
      Arrays.fill(endBytes, (byte)0);
      if (!Arrays.equals(Arrays.copyOfRange(decoded, 23, 31), endBytes)) {
        throw new DecodeException("Tag bytes in XAddress must be 0 if the address has no tag.");
      } else {
        return UnsignedInteger.ZERO;
      }
    } else {
      throw new DecodeException("Flag must be 0 to indicate no tag.");
    }
  }

  private boolean isTestAddress(byte[] decoded) {
    Objects.requireNonNull(decoded);
    byte[] prefix = Arrays.copyOfRange(decoded, 0, 2);
    if (Arrays.equals(AddressCodec.PrefixBytes.MAIN, prefix)) {
      return false;
    } else if (Arrays.equals(AddressCodec.PrefixBytes.TEST, prefix)) {
      return true;
    } else {
      throw new DecodeException("Invalid X-Address: Bad Prefix");
    }
  }

  public boolean isValidXAddress(XAddress xAddress) {
    try {
      this.decodeXAddress(xAddress);
      return true;
    } catch (Exception var3) {
      return false;
    }
  }

  public boolean isValidClassicAddress(Address address) {
    try {
      this.decodeAccountId(address);
      return true;
    } catch (Exception var3) {
      return false;
    }
  }

  private static final class PrefixBytes {
    static byte[] MAIN = new byte[]{5, 68};
    static byte[] TEST = new byte[]{4, -109};

    private PrefixBytes() {
    }
  }
}
