package na.sources.newsApi;

import na.sources.NewsSite;
import na.sources.UrnParams;
import org.springframework.stereotype.Component;

@Component("newsSite")
public class NASite implements NewsSite {
    private final static String URL;

    static {
        URL = "https://newsapi.org/v2";
    }

    @Override
    public String getFullUri(UrnParams urnParams) {
        return URL + urnParams.getUrnString();
    }
}
