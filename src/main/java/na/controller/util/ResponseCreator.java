package na.controller.util;

import org.springframework.http.ResponseEntity;
import na.pojo.News;
import na.pojo.ResultAndError;

public interface ResponseCreator {
    ResponseEntity<?> entity(Object body);
    ResultAndError<?> body(Iterable<News> newsList);
}
