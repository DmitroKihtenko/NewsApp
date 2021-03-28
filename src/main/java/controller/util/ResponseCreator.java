package controller.util;

import org.springframework.http.ResponseEntity;
import pojo.News;
import pojo.ResultAndError;

public interface ResponseCreator {
    ResponseEntity<?> entity(Object body);
    ResultAndError<?> body(Iterable<News> newsList);
}
