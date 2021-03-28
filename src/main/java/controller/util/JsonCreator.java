package controller.util;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import pojo.News;
import pojo.ResultAndError;
import service.MediaTypeLogic;

public class JsonCreator implements ResponseCreator {
    private static final Logger logger = Logger.getLogger(JsonCreator.class);

    @Override
    public ResponseEntity<String> entity(Object body) {
        String bodyString = (String) body;

        return ResponseEntity.ok().
                contentType(MediaTypeLogic.
                        createFromString(MediaType.
                                APPLICATION_JSON_VALUE)).
                contentLength(bodyString.length()).
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
                newsObject.put("title", news.getTitle());
                newsObject.put("author", news.getAuthor());
                newsObject.put("description", news.getDescription());
                newsObject.put("url", news.getUrl());
                newsObject.put("imageUrl", news.getImageUrl());

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
