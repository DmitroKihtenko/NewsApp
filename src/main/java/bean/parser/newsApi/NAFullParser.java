package bean.parser.newsApi;

import bean.parser.NewsParser;
import pojo.ResultAndError;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pojo.News;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Objects;

public class NAFullParser implements NewsParser {
    private final Logger logger = Logger.getLogger(NAFullParser.class);

    private LinkedList<News> defaultNewsList;

    public void setDefaultNewsList(LinkedList<News> defaultNewsList) {
        if(defaultNewsList == null) {
            logger.warn("Default news container parameter has null value");

            throw new IllegalArgumentException(
                    "Default news container parameter has null value"
            );
        }
        this.defaultNewsList = defaultNewsList;
    }

    @Override
    public ResultAndError<LinkedList<News>> parse(String jsonString) {
        JSONObject jsonObject;
        JSONArray newsArray;
        JSONObject authorObject;
        LinkedList<News> newsList;
        JSONObject news;
        News listNews;
        ResultAndError<LinkedList<News>> newsResult;
        newsList = Objects.requireNonNullElseGet(defaultNewsList,
                LinkedList::new);
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
                    listNews.setImageUrl((String) news.get("urlToImage"));
                } catch (MalformedURLException | URISyntaxException e) {
                    uriErrors++;
                } catch (ClassCastException | JSONException e) {
                    parseErrors++;
                }
                try {
                    authorObject = (JSONObject) news.get("author");
                    try {
                        listNews.setAuthor((String) authorObject.get("name"));
                    } catch (ClassCastException | JSONException e) {
                        parseErrors++;
                    }
                } catch (ClassCastException e) {
                    try {
                        listNews.setAuthor((String) news.get("author"));
                    } catch (ClassCastException | JSONException e1) {
                        parseErrors++;
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
