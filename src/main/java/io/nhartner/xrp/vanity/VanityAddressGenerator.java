package io.nhartner.xrp.vanity;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.VersionType;

public class VanityAddressGenerator {

  private static final AddressCodec addressCodec = AddressCodec.getInstance();

  private final WordsByLengthMap wordsMap;

  public VanityAddressGenerator(List<String> words) {
    this.wordsMap = new WordsByLengthMap(words);
  }

  private Optional<VanityAddress> findVanityMatch(String seed, String address,
      Set<String> words) {
    if (words.isEmpty()) {
      return Optional.empty();
    }
    int len = words.iterator().next().length();
    String vanityString = getVanitySubstring(address, 0, len);
    return matchFound(seed, address, words, len, vanityString);
  }

  private Optional<VanityAddress> matchFound(String seed, String address, Set<String> words,
      int len, String toMatch) {
    // doing a case insensitive match on a large word list is very slow. revert to exact match
    // (which does a much faster "contains" operation on the word set) when using large word lists.
    return words.size() > 20 || len <= 3 ?
        findUsingExactMatch(seed, address, words, toMatch) :
        findUsingCaseInsensitiveMatch(seed, address, words, toMatch);
  }

  private Optional<VanityAddress> findUsingExactMatch(String seed, String address,
      Set<String> words,
      String toMatch) {
    if (words.contains(toMatch)) {
      return Optional.of(VanityAddress.builder().address(address).vanityWord(toMatch).seed(seed)
          .build());
    }
    return Optional.empty();
  }

  private Optional<VanityAddress> findUsingCaseInsensitiveMatch(String seed, String address,
      Set<String> words,
      String toMatch) {
    return words.stream()
        .filter(word -> word.equalsIgnoreCase(toMatch))
        .findFirst()
        .map(word -> VanityAddress.builder().address(address).vanityWord(toMatch).seed(seed)
            .build());
  }

  private String getVanitySubstring(String address, int offset, int length) {
    int startIndex = 1 + offset;
    String prefix = address.substring(0, startIndex);
    char lastChar = prefix.charAt(prefix.length() - 1);
    return address.substring(1 + offset, Math.min(address.length(), length + startIndex));
  }

  public List<VanityAddress> findAddresses(int iterations) {
    // random seed generation can be a limiting factor for performance so mutate the last byte of
    // the random to get more seed permutations per random seed
    int mutations = 64;
    return IntStream.range(0, iterations / mutations)
        .mapToObj($ -> nextSeeds(mutations))
        .parallel()
        .flatMap(seeds -> seeds)
        .map(seed -> generateSeed(UnsignedByteArray.of(seed)))
        .flatMap(seed -> findVanityAddresses(seed).stream())
        .collect(Collectors.toList());
  }

  private Stream<byte[]> nextSeeds(int count) {
    try {
      byte[] base = SecureRandom.getInstance("SHA1PRNG").getSeed(16);
      return IntStream.range(0, count)
          .mapToObj(i -> {
            byte[] next = Arrays.copyOf(base, base.length);
            next[15] = (byte) ((base[15] + i)  % 64);
            return next;
          });
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private String generateSeed(UnsignedByteArray entropy) {
    return this.addressCodec.encodeSeed(entropy, VersionType.ED25519);
  }

  private List<VanityAddress> findVanityAddresses(String seed) {
    String address = AddressUtils.computeClassicAddress(seed);
    return IntStream.range(wordsMap.minLength(), Math.max(wordsMap.maxLength() + 1, 33))
        .mapToObj(wordsMap::getWordsWithLength)
        .map(words -> findVanityMatch(seed, address, words))
        .flatMap(o -> o.isPresent() ? Stream.of(o.get()) : Stream.empty())
        .collect(Collectors.toList());

  }

}
