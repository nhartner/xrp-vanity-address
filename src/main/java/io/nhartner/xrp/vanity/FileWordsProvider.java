package io.nhartner.xrp.vanity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class FileWordsProvider implements WordProvider {

  public static final URL DEFAULT_WORD_LIST = FileWordsProvider.class.getResource("/google20kwords");

  private final List<String> words;

  public FileWordsProvider(int minLength) throws IOException, URISyntaxException {
    this.words = readDefault(minLength);
  }

  public FileWordsProvider(File file) throws IOException {
    this.words = readWords(new FileInputStream(file));
  }

  public List<String> getWords() {
    return words;
  }

  private static List<String> readWords(InputStream inputStream) throws IOException {
    List<String> lines = new ArrayList<>();
    Scanner scanner = new Scanner(inputStream);
    while(scanner.hasNext()) {
      lines.add(scanner.nextLine());
    }
    return lines;
  }

  private static List<String> readDefault(int minLength) throws IOException {
    return readWords(DEFAULT_WORD_LIST.openStream())
        .stream().filter(word -> word.length() >= minLength)
        .collect(Collectors.toList());
  }

}
