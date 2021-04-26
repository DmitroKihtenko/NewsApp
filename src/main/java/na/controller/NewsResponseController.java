package na.controller;

import na.controller.services.NewsGetService;
import na.error.ErrorManager;
import na.service.Assertions;
import na.sources.SourcesParams;
import na.controller.util.NewsCreatorFactory;
import na.controller.util.ResponseCreator;
import na.error.ResponseHandleException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import na.pojo.News;
import na.pojo.ResultAndError;
import na.controller.services.NewsLookupService;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

@RestController
@ControllerAdvice
public class NewsResponseController extends NewsSearchController {
    private static final Logger logger =
            Logger.getLogger(NewsResponseController.class);

    private final ErrorManager errorManager;

    @Autowired
    public NewsResponseController(NewsLookupService newsLookupService,
                                  NewsGetService newsGetService,
                                  ErrorManager errorManager) {
        super(newsLookupService, newsGetService);
        Assertions.isNotNull(errorManager, "Error manager",
                logger);

        this.errorManager = errorManager;
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseBody
    public ResponseEntity<?> getErrorResponse(Exception e) {
        String body = Objects.requireNonNullElse(e.getMessage(),
                e.toString());

        logger.info("Sending error response with body: " +
                body);

        return ResponseEntity.status(lastErrorCode).
                contentLength(body.length()).
                contentType(new MediaType("text", "plain")).
                body(body);
    }

    @RequestMapping(value = "/getNews")
    public ResponseEntity<?> getNewsResponse(
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
                    String language,
            @RequestParam(value = "format",
                    defaultValue = "json")
                    String format)
            throws ResponseHandleException, ExecutionException,
            InterruptedException {
        logger.info("Client get news request");

        ResponseCreator creator = ((NewsCreatorFactory) beanContext.
                getBean("creatorFactory")).
                getCreator(format);

        Object body;
        ResultAndError<Iterable<News>> searchResult;
        ResultAndError<?> bodyResult;
        SourcesParams sourcesParams = (SourcesParams) beanContext.
                getBean("sourcesParams");

        sourcesParams.setCategory(category);
        sourcesParams.setCountry(country);
        sourcesParams.setLanguage(language);

        if (creator != null) {
            searchResult = searchNews(sourcesParams);

            if(!searchResult.getStatus()) {
                logger.warn("Get news error. Code: " +
                        searchResult.getErrorCode() +
                        " Message: " + searchResult.
                        getErrorMessage());
            }

            if (!searchResult.getStatus() &&
                    !errorManager.isIgnorable(searchResult.
                            getErrorCode())) {
                throw new ResponseHandleException(searchResult);
            } else {
                if(!searchResult.getStatus()) {
                    logger.info("Error with code " +
                            searchResult.getErrorCode() +
                            " ignored");
                }

                bodyResult = creator.body(searchResult.
                        getResult());

                if(!bodyResult.getStatus()) {
                    logger.warn("News response creating " +
                            "error. Code: " +
                            bodyResult.getErrorCode() +
                            " Message: " + bodyResult.
                            getErrorMessage());
                }

                if (!bodyResult.getStatus() &&
                        !errorManager.isIgnorable(bodyResult.
                                getErrorCode())) {
                    throw new ResponseHandleException(bodyResult);
                } else {
                    if(!bodyResult.getStatus()) {
                        logger.info("Error with code " +
                                bodyResult.getErrorCode() +
                                " ignored");
                    }

                    body = bodyResult.getResult();
                }
            }

        } else {
            lastErrorCode = HttpStatus.BAD_REQUEST.value();

            throw new ResponseHandleException("formatError",
                    "Cannot create response " +
                            "for format "
                            + format);
        }

        logger.info("Sending response to client");

        return creator.entity(body);
    }
}
