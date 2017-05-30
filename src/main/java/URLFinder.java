import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

public interface URLFinder {
    List<URL> getUrls() throws IOException;

    static URLFinder find(String link) {
        if (isURL(link))
            return () -> Collections.singletonList(getUrl(link));

        return () -> Files.lines(Paths.get(link))
                .map(URLFinder::getUrl)
                .collect(toList());

    }

    static URL getUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean isURL(String link) {
        Pattern p = Pattern.compile("^http[s]+:.*");
        Matcher m = p.matcher(link);
        return m.matches();
    }
}
