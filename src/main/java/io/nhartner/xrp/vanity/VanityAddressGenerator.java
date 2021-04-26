package io.nhartner.xrp.vanity;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;
import org.xrpl.xrpl4j.keypairs.KeyPairService;

public class VanityAddressGenerator {

  private static final KeyPairService KEY_PAIR_SERVICE = DefaultKeyPairService.getInstance();

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
    String toMatch = getVanitySubstring(address, len);
    return words.stream()
        .filter(word -> word.equalsIgnoreCase(toMatch))
        .findFirst()
        .map(word -> VanityAddress.builder().address(address).vanityWord(toMatch).seed(seed).build());
  }

  private String getVanitySubstring(String address, int length) {
    if (address.startsWith("r")) {
      return address.substring(1, 1 + length);
    }
    // x-address
    return address.substring(2, 2 + length);
  }

  public List<VanityAddress> findAddresses(int iterations) {
    return IntStream.range(0, iterations)
        .parallel()
        .mapToObj($ -> KEY_PAIR_SERVICE.generateSeed())
        .flatMap(seed -> findVanityAddresses(seed).stream())
        .collect(Collectors.toList());
  }

  private List<VanityAddress> findVanityAddresses(String seed) {
    String address = AddressUtils.computeClassicAddress(seed);
    return IntStream.range(wordsMap.minLength(), wordsMap.maxLength() + 1)
        .mapToObj(wordsMap::getWordsWithLength)
        .map(words -> findVanityMatch(seed, address, words))
        .flatMap(o -> o.isPresent() ? Stream.of(o.get()) : Stream.empty())
        .collect(Collectors.toList());

  }

}