import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;

@Slf4j
@Getter
public class ConsoleReader {

    public static final int timeSpent = 0x01;   //-v
    public static final int pageSize  = 0x02;   //-c
    public static final int wordCount = 0x04;   //-w
    public static final int sentences = 0x08;   //-e

    private List<String> keyWords;
    private int params;
    private String path;
    private List<CompletableFuture<ParseResult>> results;

    private ConsoleReader() {
    }

    public static ConsoleReader init(String[] args) {
        ConsoleReader parser = new ConsoleReader();
        parser.keyWords = generateKeyWords(args[1]);
        parser.params = readParams(args);
        parser.path = args[0];
        parser.results = new LinkedList<>();
        return parser;
    }

    public void start() throws IOException {
        URLFinder.find(path).getUrls()
                .forEach(url ->
                        results.add(CompletableFuture
                                .supplyAsync(() -> new URLParser(url, keyWords).parse())
                                .exceptionally(th -> {
                                    log.error("URL couldn't be parsed  - " + url);
                                    return null;
                                })));
    }


    public List<ParseResult> getResults(){
        return CompletableFuture.allOf(results.toArray(new CompletableFuture[0]))
                .thenApplyAsync(x -> results.stream()
                        .map(ConsoleReader::getOrNull)
                        .filter(Objects::nonNull)
                        .collect(toList())).join();
    }

    private static <T> T getOrNull(CompletableFuture<T> f) {
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e1) {
            log.warn("CompletableFuture error", e1);
            return null;
        }
    }

    private static List<String> generateKeyWords(String words) {
        return Arrays.stream(words.split(","))
                .map(String::toLowerCase)
                .collect(toList());
    }

    private static int readParams(String[] args) {
        int start = 2;
        int params = 0;
        while (start < args.length && args[start] != null) {
            switch (args[start]) {
                case "-v":
                    params |= timeSpent;
                    break;
                case "-c":
                    params |= pageSize;
                    break;
                case "-w":
                    params |= wordCount;
                    break;
                case "-e":
                    params |= sentences;
                    break;
                default:
                    throw new RuntimeException("Unknown keys");
            }
            start++;
        }
        return params;
    }


    public boolean hasParams(int params) {
        return (this.params & params) == params;
    }


    public StringBuilder printAllResults(){
        StringBuilder stringBuilder = new StringBuilder();
        for (ParseResult result : getResults())
            stringBuilder.append('\n').append(printResult(result));
        return stringBuilder;
    }

    private StringBuilder printResult(ParseResult result) {
        StringBuilder str = new StringBuilder();
        if (result.getUrl() == null)
            str.append("Global result: \n");
        else
            str.append("URL: ").append(result.getUrl()).append('\n');

        if (hasParams(ConsoleReader.wordCount)) {
            str.append("Number of provided words: ");
            for (Map.Entry<String, Integer> pair : result.getResult().entrySet()) {
                str.append(pair.getKey()).append('=').append(pair.getValue()).append(" ");
            }
        } else {
            str.append("Founded words: ");
            for (Map.Entry<String, Integer> pair : result.getResult().entrySet()) {
                if (pair.getValue() > 0)
                    str.append(pair.getKey()).append(" ");
            }
        }
        str.append('\n');
        if (hasParams(ConsoleReader.sentences)) {
            str.append("Sentences with key words:\n");
            result.getSentences().forEach(x -> str.append(" - ").append(x).append('\n'));
        }
        if (hasParams(ConsoleReader.pageSize))
            str.append("Size: ").append(result.getPageSize()).append('\n');
        if (hasParams(ConsoleReader.timeSpent) && result.getUrl() != null)
            str.append("Time spent: ").append(result.getTimeSpent()).append(" ms").append('\n');
        return str;
    }

    public StringBuilder printGlobalResult(){
        ParseResult result = new ParseResult();
        Map<String, Integer> map = result.getResult();
        keyWords.forEach(word -> map.put(word, 0));
        for (ParseResult pR : getResults())
            result.join(pR);
        return printResult(result);
    }


    public static String getHelpMsg() {
        return "HELP: \n"+
                "java -jar scraper.jar [path] [keyWords] [keys] \n" +
                "path    : URL or Path to file with URL\n" +
                "keyWords: Word or list of words with “,” delimiter\n" +
                "keys: \n" +
                "  -v : get time spent on parsing\n" +
                "  -c : get count number of characters of web page\n" +
                "  -w : count number of provided word(s) occurrence on webpage(s)\n" +
                "  -e : get all sentences which contain given key words";
    }
}