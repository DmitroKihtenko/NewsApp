package controller;

import bean.error.ErrorManager;
import bean.sources.SourcesParams;
import controller.util.ResponseCreator;
import controller.util.NewsCreatorFactory;
import exceptions.ResponseHandleException;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pojo.ContextSingleton;
import pojo.News;
import pojo.ResultAndError;
import service.MediaTypeLogic;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@RestController
public class NewsFormatController extends NewsSearchController {
    private static final Logger logger =
            Logger.getLogger(NewsFormatController.class);

    private final ErrorManager errorManager;

    NewsFormatController(ErrorManager errorManager) {
        if(errorManager == null) {
            logger.fatal("Error manager parameter has null value");

            throw new IllegalArgumentException(
                    "Error manager parameter has null value"
            );
        }
        this.errorManager = errorManager;
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseBody
    public ResponseEntity<?> getErrorResponse(HttpServletRequest request, Exception e) {
        String body = e.getMessage();

        return ResponseEntity.status(lastErrorCode).
                contentLength(body.length()).
                contentType(new MediaType("text", "plain")).
                body(body);
    }

    @RequestMapping(value = "/getNews")
    public ResponseEntity<?> getNews(HttpServletRequest request,
                                          @RequestParam(
                                                  value = "category",
                                                  defaultValue = "all")
                                                  String category,
                                          @RequestParam(
                                                  value = "country",
                                                  defaultValue = "all")
                                                 String country,
                                          @RequestParam(
                                                  value = "language",
                                                  defaultValue = "all")
                                                 String language) throws ResponseHandleException {
        MediaType requestMediaType;

        try {
            requestMediaType = MediaTypeLogic.
                    createFromString(request.getHeader("Accept"));
        } catch (Exception e) {
            requestMediaType = MediaTypeLogic.
                    createFromString("application/" +
                            "vnd.openxmlformats-officedocument." +
                            "wordprocessingml.document");
        }

        ResponseCreator creator =
                NewsCreatorFactory.getCreator(requestMediaType);
        Object body;
        ResultAndError<Iterable<News>> searchResult;
        ResultAndError<?> bodyResult;
        SourcesParams sourcesParams = (SourcesParams)
                ContextSingleton.getInstance().
                        getBean("NASourcesParams");

        sourcesParams.setCategory(category);
        sourcesParams.setCountry(country);
        sourcesParams.setLanguage(language);

        if(creator != null) {
            searchResult = getNewsList(sourcesParams);

            if(!searchResult.getStatus() &&
                    !errorManager.isIgnorable(searchResult.
                            getErrorCode())) {
                throw new ResponseHandleException(searchResult);
            } else {
                bodyResult = creator.body(searchResult.
                        getResult());
                if(!bodyResult.getStatus() &&
                        !errorManager.isIgnorable(bodyResult.
                                getErrorCode())) {
                    throw new ResponseHandleException(bodyResult);
                } else {
                    body = bodyResult.getResult();
                }
            }

        } else {
            lastErrorCode = 500;

            throw new ResponseHandleException("formatError",
                    "Cannot create response " +
                            "for media type "
                            + requestMediaType);
        }

        return creator.entity(body);
    }
}
