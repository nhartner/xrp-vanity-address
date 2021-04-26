package io.nhartner.xrp.vanity;

import org.immutables.value.Value.Immutable;

@Immutable
public interface VanityAddress {

  static ImmutableVanityAddress.Builder builder() {
    return ImmutableVanityAddress.builder();
  }

  String vanityWord();

  String address();

  String seed();

}
