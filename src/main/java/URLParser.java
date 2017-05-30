import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
public class URLParser {

    private static final Pattern ANY_TAG = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>");
    private static final Pattern BODY_TAG = Pattern.compile(".*<body(\"[^\"]*\"|'[^']*'|[^'\">])*>(.*)");

    private ParseResult parseResult;
    private URL url;

    public URLParser(URL url, List<String> keyWords) {
        this.url = url;
        parseResult = new ParseResult();
        Map<String, Integer> map = new HashMap<>();
        keyWords.forEach(s -> map.put(s, 0));
        parseResult.setResult(map);
    }

    public ParseResult parse() {
        long startTime = System.currentTimeMillis();
        long size = 0;

        try (InputStream inputStream = url.openStream();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream, "UTF-8"))) {
            String str;
            boolean isBody = false;
            while ((str = reader.readLine()) != null) {
                size+=str.length();
                if (!isBody) {
                    Matcher m = BODY_TAG.matcher(str);
                    if (m.matches()) {
                        str = m.group(2);
                        isBody = true;
                    } else
                        continue;
                }
                findMatches(str);
            }
        } catch (IOException e) {
            String message = "URL path isn't correct -  " + url;
            throw new RuntimeException(message);
        }
        parseResult.setTimeSpent(System.currentTimeMillis()-startTime);
        parseResult.setPageSize(size);
        parseResult.setUrl(url);
        return parseResult;
    }

    void findMatches(String str) {
        Matcher matcher = ANY_TAG.matcher(str);
        String line = matcher.replaceAll(".");

        String[] sentences = line.split("[.?!]+");
        for (String s : sentences) {
            parseSentence(s);
        }
    }

    void parseSentence(String s) {
        String[] words = s.split("[^a-zA-zà-ÿÀ-ß0-9]");
        boolean added = false;
        for (String word : words) {
            if (hasMatch(word) && !added) {
                parseResult.getSentences().add(s.trim());
                added = true;
            }
        }
    }

    boolean hasMatch(String word) {
        return parseResult.getResult()
                .computeIfPresent(word.toLowerCase(), (s1, count) -> count + 1) != null;
    }
}
