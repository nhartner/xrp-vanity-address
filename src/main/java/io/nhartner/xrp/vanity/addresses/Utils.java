package io.nhartner.xrp.vanity.addresses;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

  private static final ThreadLocal<MessageDigest> threadLocalDigest = new ThreadLocal<>();

  /**
   * See {@link io.nhartner.xrp.vanity.addresses.Utils#doubleDigest(byte[], int, int)}.
   *
   * @param input A byte array to double digest.
   * @return The SHA-256 hash of the SHA-256 hash of the given input.
   */
  public static byte[] doubleDigest(byte[] input) {
    return doubleDigest(input, 0, input.length);
  }

  /**
   * Calculates the SHA-256 hash of the given byte range, and then hashes the resulting hash again. This is
   * standard procedure in XRPL. The resulting hash is in big endian form.
   *
   * @param input  A byte array to double digest.
   * @param offset The beginning index of the input to digest.
   * @param length The length of the input to digest.
   * @return The SHA-256 hash of the SHA-256 hash of the given input.
   */
  public static byte[] doubleDigest(byte[] input, int offset, int length) {
    MessageDigest digest = digest();
    digest.reset();
    digest.update(input, offset, length);
    byte[] first = digest.digest();
    return digest.digest(first);
  }

  private static MessageDigest digest() {
    MessageDigest digest = threadLocalDigest.get();
    if (digest == null) {
      try {
        digest = MessageDigest.getInstance("SHA-256");
        threadLocalDigest.set(digest);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);  // Can't happen.
      }
    }
    return digest;
  }

}
