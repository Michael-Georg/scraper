import lombok.Data;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class ParseResult {
    private Map<String, Integer> result;
    private List<String> sentences;
    private long timeSpent;
    private long pageSize;
    private URL url;

    public ParseResult() {
        result = new HashMap<>();
        sentences = new LinkedList<>();
    }

    @Override
    public String toString() {
        return "URL: " + url + "\n"+
                "result=" + result + '\n' +
                "sentences=" + sentences + '\n'+
                "timeSpent=" + timeSpent + '\n' +
                "pageSize=" + pageSize;
    }

    public void join(ParseResult pR) {
        for (Map.Entry<String, Integer> pair : pR.getResult().entrySet())
            this.result.computeIfPresent(pair.getKey(), (s, count) ->count+=pair.getValue());
        this.sentences.addAll(pR.getSentences());
        this.pageSize+=pR.getPageSize();
        this.timeSpent+=pR.getTimeSpent();
        this.url = null;
    }
}
