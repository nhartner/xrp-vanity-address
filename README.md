# XRP Vanity Address Generator

Java app to generate XRP Ledger vanity addresses. Inspired by Wietse
Wind's [xrp-vanity-generator](https://github.com/WietseWind/xrp-vanity-generator)
written in Javascript. The Java version has the advantage of being roughly 75x faster and therefore
capable of finding a desired vanity address 75x faster.

## Ways to run

1. ### Targeted Word Mode
   In this mode, only addresses starting with the provided words will be considered.
   ```shell
   ./vanity-generator.sh crypto hodl xrp
   ```

1. ### Roulette Mode

   In this mode, vanity addresses are generated against a list of the top 20,000 search terms
   according to Google.
   ```shell
   ./vanity-generator.sh
   ```
   By default, the word list only uses words that are 4 letters or longer. This can be adjusted by
   passing a numeric value. For example, to only consider words with 5 or more letters use:
   ```shell
   ./vanity-generator.sh 5
   ```
   
## Generating an offline executable jar

You can generate an executable jar that can be copied and executed on an offline device.
The offline device only needs Java to be installed.

To build the executable jar, run:
```shell
./mvnw package
```

The executable jar will be created under `target/vanity-generator.jar`

The jar can be executed using `java -jar path/to/vanity-generator.jar`. For example:
```shell
java -jar target/vanity-generator.jar crypto hodl xrpd
```

