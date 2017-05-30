import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConsoleReaderTest {

    public static final String URL = "https://docs.oracle.com/en/java/";
    private static ConsoleReader cParser;

    @BeforeAll
    static void setup(){
       cParser = ConsoleReader.init(new String[]{"src\\main\\resources\\test.txt", "one,two,three", "-c", "-e"});
    }

    @Test
    void wordParseCorrect() throws Exception {
        assertThat(cParser.getKeyWords(), containsInAnyOrder("one", "two", "three"));
    }

    @Test
    void keysParseCorrect() throws Exception {
        assertTrue(cParser.hasParams(ConsoleReader.pageSize | ConsoleReader.sentences));
        assertFalse(cParser.hasParams(ConsoleReader.timeSpent));
    }

    @Test
    void URLFinderCorrectWorkWithPath() throws IOException {
        List<URL> urls = URLFinder.find("src\\main\\resources\\test.txt").getUrls();
        assertThat(urls.size(), is(5));
        assertThat(urls, hasItem(new URL(URL)));
    }

    @Test
    void hasMatchTest() throws MalformedURLException {
        URLParser urlParser = getUrlParser();
        assertTrue(urlParser.hasMatch("home"));
        assertTrue(urlParser.hasMatch("hOMe"));
        assertFalse(urlParser.hasMatch("123"));
    }

    @Test
    void parseSentenceTest() throws MalformedURLException {
        URLParser urlParser = getUrlParser();
        String expected = "HAsdhasdh,asdwdax HOME+JAVA      java";
        urlParser.parseSentence(expected);
        String actual = urlParser.getParseResult().getSentences().get(0);

        assertThat(actual, is(expected));

        Map<String, Integer> result = urlParser.getParseResult().getResult();
        assertThat(result.get("java"),is(2));
        assertThat(result.get("home"),is(1));
        assertThat(result.get("facebook"),is(0));
    }

    @Test
    void URLParserTest() throws IOException, InterruptedException, ExecutionException {
        ConsoleReader reader = ConsoleReader.init(new String[]{"src\\main\\resources\\test.txt", "java,home,facebook", "-c"});
        reader.start();
        reader.getResults().forEach(System.out::println);
    }

    @Test
    void parseTest() throws MalformedURLException {
        URLParser parser = getUrlParser();
        ParseResult parse = parser.parse();
        Map<String, Integer> result = parse.getResult();
        assertThat(result.get("home"), is(2));
        assertThat(result.get("java"), greaterThan(20));
        assertThat(result.get("facebook"), is(0));
        assertThat(parse.getTimeSpent(), greaterThan(0L));
        assertThat(parse.getPageSize(), greaterThan(10000L));
        System.out.println(parse.toString());
    }

    @Test
    void findMatchesTest() throws MalformedURLException {
        URLParser parser = getUrlParser();
        parser.findMatches("<div class=\"java\" ng-asd=\'1+1\'> Java.really java!?<p>java java java<p> JAVA</div>");
        List<String> sentences = parser.getParseResult().getSentences();
        assertThat(sentences.size(), is(4));
        assertThat(sentences, hasItem("Java"));
        assertThat(sentences, hasItem("really java"));
        assertThat(sentences, hasItem("really java"));

    }

    private URLParser getUrlParser() throws MalformedURLException {
        return new URLParser(new URL(URL), Arrays.asList("home", "java", "facebook"));
    }
}