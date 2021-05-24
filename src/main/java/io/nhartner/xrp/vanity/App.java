package io.nhartner.xrp.vanity;

import com.google.common.base.Joiner;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

public class App {

  public static void main(String[] args) throws IOException, URISyntaxException {
    List<String> words = getWords(args);
    VanityAddressGenerator generator = new VanityAddressGenerator(words);
    int iterations = 4096;
    while (true) {
      long start = System.currentTimeMillis();
      generator.findAddresses(iterations).forEach(App::print);
      long end = System.currentTimeMillis();
      System.out
          .printf("Analyzing %f addresses/second\r", iterations * 1.0 / (end - start) * 1000);

      iterations = adjustIterations(iterations, start, end);
    }
  }

  /**
   * Calculates how many iterations to run to target a 1 second cycle.
   * @param iterations
   * @param lastStart
   * @param lastEnd
   * @return
   */
  private static int adjustIterations(int iterations, long lastStart, long lastEnd) {
    if ((lastEnd - lastStart) < 1000) {
      iterations += 2048;
    } else {
      iterations -= 1024;
    }
    return iterations;
  }

  private static List<String> getWords(String[] args) throws IOException, URISyntaxException {
    Optional<Integer> maybeMinLength = getMinLength(args);
    if (args.length == 0 || maybeMinLength.isPresent()) {
      int minLength = maybeMinLength.orElse(4);
      System.out.println(
          "Finding vanity addresses using Google top 20K search words with length >= "
              + minLength);
      return new FileWordsProvider(minLength).getWords();
    }
    System.out.println("Finding vanity addresses using word list: " + Joiner.on(", ").join(args));
    return Arrays.asList(args);
  }

  private static void print(VanityAddress address) {
    System.out.printf("Address: %s \tSeed: %s\n",
        PrintUtils.highlight(address.address(), address.vanityWord()),
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
