package io.nhartner.xrp.vanity.addresses.exceptions;

public class EncodingFormatException extends RuntimeException {
  public EncodingFormatException() {
    super();
  }

  public EncodingFormatException(String message) {
    super(message);
  }
}
