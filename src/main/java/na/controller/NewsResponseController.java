package na.controller;

import na.error.ErrorManager;
import na.sources.SourcesParams;
import na.controller.util.NewsCreatorFactory;
import na.controller.util.ResponseCreator;
import na.error.ResponseHandleException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import na.pojo.News;
import na.pojo.ResultAndError;
import na.pojo.MediaTypeLogic;
import na.controller.services.NewsLookupService;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@RestController
@ControllerAdvice
public class NewsResponseController extends NewsSearchController {
    private static final Logger logger =
            Logger.getLogger(NewsResponseController.class);

    private ErrorManager errorManager;

    @Autowired
    public NewsResponseController(NewsLookupService newsLookupService,
                                  ErrorManager errorManager,
                                  @Value("${newsLookupThreads}")
                                              int newsLookupThreads) {
        super(newsLookupService, newsLookupThreads);
        if(errorManager == null) {
            logger.error("Error manager parameter has null value");

            throw new IllegalArgumentException(
                    "Error manager parameter has null value"
            );
        }
        this.errorManager = errorManager;
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseBody
    public ResponseEntity<?> getErrorResponse(Exception e) {
        String body = Objects.requireNonNullElse(e.getMessage(), e.toString());

        return ResponseEntity.status(lastErrorCode).
                contentLength(body.length()).
                contentType(new MediaType("text", "plain")).
                body(body);
    }

    @RequestMapping(value = "/getNews")
    public ResponseEntity<?> getNewsResponse(HttpServletRequest request,
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
                                             String language) throws ResponseHandleException, ExecutionException, InterruptedException {
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

        ResponseCreator creator = ((NewsCreatorFactory) beanContext.
                getBean("creatorFactory")).getCreator(requestMediaType);
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

            if (!searchResult.getStatus() &&
                    !errorManager.isIgnorable(searchResult.
                            getErrorCode())) {
                throw new ResponseHandleException(searchResult);
            } else {
                bodyResult = creator.body(searchResult.
                        getResult());
                if (!bodyResult.getStatus() &&
                        !errorManager.isIgnorable(bodyResult.
                                getErrorCode())) {
                    throw new ResponseHandleException(bodyResult);
                } else {
                    body = bodyResult.getResult();
                }
            }

        } else {
            lastErrorCode = HttpStatus.BAD_REQUEST.value();

            throw new ResponseHandleException("formatError",
                    "Cannot create response " +
                            "for media type "
                            + requestMediaType);
        }

        return creator.entity(body);
    }
}
