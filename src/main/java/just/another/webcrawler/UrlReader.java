package just.another.webcrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static java.util.stream.Collectors.joining;

/**
 * Simple class that reads the contents of a URL. Having this in it's own class allows for mocking in test.
 */
public class UrlReader {

    public String read(String url) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(joining("\n"));
        }
    }
}
