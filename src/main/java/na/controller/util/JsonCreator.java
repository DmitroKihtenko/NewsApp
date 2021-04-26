package na.controller.util;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import na.pojo.News;
import na.pojo.ResultAndError;

import java.nio.charset.StandardCharsets;

@Component("jsonCreator")
public class JsonCreator implements ResponseCreator {
    private static final Logger logger =
            Logger.getLogger(JsonCreator.class);

    @Override
    public ResponseEntity<String> entity(Object body) {
        String bodyString = (String) body;

        return ResponseEntity.ok().
                contentType(MediaType.APPLICATION_JSON).
                contentLength(bodyString.getBytes(
                        StandardCharsets.UTF_8).length).
                body(bodyString);
    }

    @Override
    public ResultAndError<String> body(Iterable<News> newsList) {
        ResultAndError<String> rae;
        try {
            logger.info("Starting creating response body");

            JSONObject object = new JSONObject();
            JSONArray array = new JSONArray();
            JSONObject newsObject;

            for(News news : newsList) {
                newsObject = new JSONObject();
                if(news.getTitle() != null) {
                    newsObject.put("title", news.getTitle());
                }
                if(news.getAuthor() != null) {
                    newsObject.put("author", news.getAuthor());
                }
                if(news.getDescription() != null) {
                    newsObject.put("description", news.getDescription());
                }
                if(news.getUrl() != null) {
                    newsObject.put("url", news.getUrl());
                }
                if(news.getImageUrl() != null) {
                    newsObject.put("imageUrl", news.getImageUrl());
                }

                array.put(newsObject);
            }

            object.put("newsList", array);

            rae = new ResultAndError<>(object.toString());
        } catch (Exception e) {
            logger.error(e.getMessage());

            rae = new ResultAndError<>("responseCreatingError",
                    e.getMessage());
        }

        logger.info("Successfully created");

        return rae;
    }
}
