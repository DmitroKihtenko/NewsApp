package na.controller;

import na.controller.services.NewsGetService;
import na.parser.NewsParser;
import na.sources.IdParams;
import na.sources.UrnParams;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import na.pojo.News;
import na.pojo.ResultAndError;
import na.controller.services.NewsLookupService;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class NewsSearchController {
    private final static Logger logger =
            Logger.getLogger(NewsSearchController.class);

    private final NewsLookupService newsLookupService;
    private final NewsGetService newsGetService;

    protected ApplicationContext beanContext;
    protected int lastErrorCode;

    public NewsSearchController(NewsLookupService newsLookupService,
                                NewsGetService newsGetService) {
        if(newsLookupService == null) {
            logger.error("News lookup service has null value");

            throw new IllegalArgumentException(
                    "News lookup service has null value"
            );
        }
        if(newsGetService == null) {
            logger.error("News get service has null value");

            throw new IllegalArgumentException(
                    "News get service has null value"
            );
        }
        this.newsLookupService = newsLookupService;
        this.newsGetService = newsGetService;

        lastErrorCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    @Autowired
    public void setBeanContext(ApplicationContext applicationContext) {
        this.beanContext = applicationContext;
    }

    protected ResultAndError<Iterable<News>> searchNews(UrnParams
                                                             sourcesParams)
            throws InterruptedException, ExecutionException {
        LinkedList<News> newsList = new LinkedList<>();
        ResultAndError<Iterable<News>> newsResult =
                new ResultAndError<>(newsList);
        ResponseEntity<String> resultsValueJson;
        ResponseEntity<String> rawSourcesJson;

        ResultAndError<List<String>> idResult;
        ResultAndError<Integer> pagesResult;
        ResultAndError<Iterable<News>> result;
        int pagesAmount;

        IdParams idParams = (IdParams) beanContext.
                getBean("idParams");
        NewsParser sourcesIdParser = (NewsParser) beanContext.
                getBean("sourcesIdParser");
        NewsParser resultsValueParser = (NewsParser) beanContext.
                getBean("resultsValueParser");

        rawSourcesJson = newsLookupService.lookup(sourcesParams);
        if(rawSourcesJson.getStatusCode().value() >=
                HttpStatus.BAD_REQUEST.value()) {
            this.lastErrorCode = rawSourcesJson.
                    getStatusCode().value();
        }
        idResult = (ResultAndError<List<String>>) sourcesIdParser.
                parse(rawSourcesJson.getBody());
        if(!idResult.getStatus()) {
            newsResult.setError(idResult.getErrorCode(),
                    idResult.getErrorMessage());
            return newsResult;
        }
        idParams.setIdsList(idResult.getResult());

        IdParams sourcesIdsClone = idParams.clone();
        sourcesIdsClone.setPageSize(1);
        resultsValueJson = newsLookupService.lookup(sourcesIdsClone);
        if(resultsValueJson.getStatusCode().value() >=
                HttpStatus.BAD_REQUEST.value()) {
            lastErrorCode = resultsValueJson.getStatusCode().value();
        }
        pagesResult = (ResultAndError<Integer>)
                resultsValueParser.parse(resultsValueJson.getBody());
        if(!pagesResult.getStatus()) {
            newsResult.setError(pagesResult.getErrorCode(),
                    pagesResult.getErrorMessage());
            return newsResult;
        }

        pagesAmount = pagesResult.getResult() / idParams.getPageSize();
        if (pagesResult.getResult() % idParams.getPageSize() != 0) {
            pagesAmount++;
        }

        result = newsGetService.asyncRequest(idParams, pagesAmount);
        if(newsGetService.getLastErrorCode() != null) {
            lastErrorCode = newsGetService.getLastErrorCode();
        }
        return result;
    }
}
