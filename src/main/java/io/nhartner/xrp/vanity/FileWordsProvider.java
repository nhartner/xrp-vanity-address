package io.nhartner.xrp.vanity;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileWordsProvider implements WordProvider {

  public static final URL DEFAULT_WORD_LIST = Resources
      .getResource(FileWordsProvider.class, "/google20kwords");

  private final List<String> words;

  public FileWordsProvider(int minLength) throws IOException, URISyntaxException {
    this.words = readDefault(minLength);
  }

  public FileWordsProvider(Path path) throws IOException {
    this.words = readWords(path);
  }

  public List<String> getWords() {
    return words;
  }

  private static List<String> readWords(Path path) throws IOException {
    return Files.readAllLines(path);
  }

  private static List<String> readDefault(int minLength) throws IOException, URISyntaxException {
    return readWords(Paths.get(DEFAULT_WORD_LIST.toURI()))
        .stream().filter(word -> word.length() >= minLength)
        .collect(Collectors.toList());
  }

}
