package io.nhartner.xrp.vanity;

import com.google.common.base.Joiner;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class App {

  public static void main(String[] args) throws IOException, URISyntaxException {
    List<String> words = getWords(args);
    VanityAddressGenerator generator = new VanityAddressGenerator(words);
    int iterations = 100_000;
    while (true) {
      long start = System.currentTimeMillis();
      generator.findAddresses(iterations).forEach(App::print);
      long end = System.currentTimeMillis();
      System.out.printf("Analyzing %f addresses/second\r", iterations * 1.0 / (end - start) * 1000);
    }
  }

  private static List<String> getWords(String[] args) throws IOException, URISyntaxException {
    Optional<Integer> maybeMinLength = getMinLength(args);
    if (args.length == 0 || maybeMinLength.isPresent()) {
      int minLength = maybeMinLength.orElse(4);
      System.out.println("Finding vanity addresses using random words with length >= " + minLength);
      return new FileWordsProvider(minLength).getWords();
    }
    System.out.println("Finding vanity addresses using word list: " + Joiner.on(", ").join(args));
    return Arrays.asList(args);
  }

  private static void print(VanityAddress address) {
    System.out.printf("%s -> address: %s, seed: %s\n", address.vanityWord(), address.address(),
        address.seed());
  }

  private static Optional<Integer> getMinLength(String[] args) {
    if (args.length != 1) {
      return Optional.empty();
    }
    try {
      return Optional.of(Integer.parseInt(args[0]));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

}
