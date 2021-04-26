package io.nhartner.xrp.vanity;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class WordsByLengthMap {

  private final Map<Integer, Set<String>> wordsByLength;

  public WordsByLengthMap(List<String> words) {
    this.wordsByLength = toNormalizedWordByLength(words);
  }

  public int minLength() {
    return wordsByLength.keySet().stream().min(Integer::compareTo).orElse(-1);
  }

  public int maxLength() {
    return wordsByLength.keySet().stream().max(Integer::compareTo).orElse(0);
  }

  public Set<String> getWordsWithLength(int length) {
    return Optional.ofNullable(wordsByLength.get(length)).orElse(Collections.EMPTY_SET);
  }

  public static Map<Integer, Set<String>> toNormalizedWordByLength(List<String> words) {
//    List<String> lower = words.stream()
//        .map(String::toLowerCase)
//        .map(WordsByLengthMap::toBase58Alphabet)
//        .collect(Collectors.toList());

    List<String> upper = words.stream()
        .map(String::toUpperCase)
        .map(WordsByLengthMap::toBase58Alphabet)
        .collect(Collectors.toList());

    List<String> all = new ImmutableList.Builder<String>()
        .addAll(upper)
//        .addAll(lower)
        .build();

    return all.stream()
        .collect(Collectors.groupingBy($ -> $.length(), Collectors.toSet()));
  }

  private static String toBase58Alphabet(String word) {
    return word.replace("l", "L")
        .replace("0", "o")
        .replace("O", "o")
        .replace("I", "i");
  }

}
