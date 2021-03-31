package na.parser.newsApi;

import na.parser.NewsParser;
import org.springframework.stereotype.Component;
import na.pojo.ResultAndError;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import na.pojo.News;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.LinkedList;

@Component("newsFullParser")
public class NAFullParser implements NewsParser {
    private final Logger logger = Logger.getLogger(NAFullParser.class);

    @Override
    public ResultAndError<LinkedList<News>> parse(String jsonString) {
        JSONObject jsonObject;
        JSONArray newsArray;
        JSONObject authorObject;
        LinkedList<News> newsList;
        JSONObject news;
        News listNews;
        ResultAndError<LinkedList<News>> newsResult;
        newsList = new LinkedList<>();
        newsResult = new ResultAndError<>(newsList);

        int uriErrors = 0;
        int parseErrors = 0;

        logger.info("Starting parsing news attributes");

        try {
            jsonObject = new JSONObject(jsonString);

            String status = (String) jsonObject.get("status");

            if(!status.equals("ok")) {
                newsResult.setError((String) jsonObject.
                        get("code"), (String) jsonObject.
                        get("message"));
                return newsResult;
            }

            newsArray = (JSONArray) jsonObject.get("articles");

            for(int counter = 0; counter < newsArray.length(); counter++) {
                news = (JSONObject) newsArray.get(counter);

                listNews = new News();
                newsList.add(listNews);

                try {
                    listNews.setTitle((String) news.get("title"));
                    listNews.setDescription((String) news.get("description"));
                    listNews.setUrl((String) news.get("url"));
                } catch (MalformedURLException | URISyntaxException e) {
                    uriErrors++;
                } catch (ClassCastException | JSONException e) {
                    parseErrors++;
                }
                try {
                    listNews.setImageUrl((String) news.get("urlToImage"));
                } catch (MalformedURLException | URISyntaxException e) {
                    uriErrors++;
                } catch (ClassCastException | JSONException e) {

                }
                try {
                    authorObject = (JSONObject) news.get("author");
                    try {
                        listNews.setAuthor((String) authorObject.get("name"));
                    } catch (ClassCastException | JSONException e) {
                    }
                } catch (ClassCastException e) {
                    try {
                        listNews.setAuthor((String) news.get("author"));
                    } catch (ClassCastException | JSONException e1) {
                    }
                }
            }
        } catch (JSONException | ClassCastException e) {
            logger.error("Parse error: " + e.getMessage());

            newsResult.setError("parseError",
                    e.getMessage());
        }

        logger.info("Successfully parsed");

        if(uriErrors != 0) {
            logger.info("It has been catch " + uriErrors +
                    " news parse errors");
        }

        if(parseErrors != 0) {
            logger.info("It has been catch " + parseErrors +
                    " news parse errors");
        }

        return newsResult;
    }
}
