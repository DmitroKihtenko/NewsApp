package na.controller;

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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class NewsSearchController {
    private final static Logger logger =
            Logger.getLogger(NewsSearchController.class);

    private final NewsLookupService newsLookupService;
    private int newsLookupThreads;

    protected ApplicationContext beanContext;
    protected int lastErrorCode;

    public NewsSearchController(NewsLookupService newsLookupService,
                                int newsLookupThreads) {
        if(newsLookupService == null) {
            logger.error("News lookup na.service has null value");

            throw new IllegalArgumentException(
                    "News lookup na.service has null value"
            );
        }
        this.newsLookupService = newsLookupService;

        if(newsLookupThreads <= 0) {
            logger.error("Images lookup threads amount has " +
                    "non-positive value");

            throw new IllegalArgumentException(
                    "Images lookup threads amount has " +
                            "non-positive value"
            );
        }
        this.newsLookupThreads = newsLookupThreads;

        lastErrorCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    @Autowired
    public void setBeanContext(ApplicationContext applicationContext) {
        this.beanContext = applicationContext;
    }

    protected ResultAndError<Iterable<News>>
    asyncRequest(IdParams idParams, int pagesAmount) throws ExecutionException, InterruptedException {
        LinkedList<News> generalList = new LinkedList<>();
        ResultAndError<Iterable<News>> rae =
                new ResultAndError<>(generalList);
        ResultAndError<Iterable<News>> newsJsonResult;
        ResponseEntity<String> requestResult;
        CompletableFuture<ResponseEntity<String>>[] threadResults =
                new CompletableFuture[newsLookupThreads];
        int threadIter = 0;
        NewsParser newsFullParser = (NewsParser) beanContext.
                getBean("newsFullParser");
        IdParams cloneIdParams;

        logger.info("Starting send asynchronous news requests " +
                "with " + newsLookupThreads + " threads");

        for(int page = 1; page <= pagesAmount; page++) {
            cloneIdParams = idParams.clone();
            cloneIdParams.setPage(page);
            threadResults[threadIter] =
                    newsLookupService.lookup(cloneIdParams);

            if(threadIter >= newsLookupThreads - 1 ||
                    page >= pagesAmount) {
                CompletableFuture.allOf(Arrays.copyOf(
                        threadResults, threadIter + 1)).
                        join();

                for(int threadRead = 0; threadRead <=
                        threadIter; threadRead++) {
                    requestResult = threadResults[threadRead].get();

                    if(requestResult.getStatusCode().value() >=
                            HttpStatus.BAD_REQUEST.value()) {
                        this.lastErrorCode =
                                requestResult.getStatusCode().value();
                    }
                    newsJsonResult = (ResultAndError<Iterable<News>>)
                            newsFullParser.parse(requestResult.getBody());

                    if(!newsJsonResult.getStatus()) {
                        rae.setError(newsJsonResult.getErrorCode(),
                                newsJsonResult.getErrorMessage());
                        return rae;
                    }

                    for(News news : newsJsonResult.getResult()) {
                        generalList.add(news);
                    }
                }
                threadIter = 0;
            } else {
                threadIter++;
            }
        }

        logger.info("Successfully requested");

        return rae;
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
        int pagesAmount;

        IdParams idParams = (IdParams) beanContext.
                getBean("idParams");
        NewsParser sourcesIdParser = (NewsParser) beanContext.
                getBean("sourcesIdParser");
        NewsParser resultsValueParser = (NewsParser) beanContext.
                getBean("resultsValueParser");

        rawSourcesJson = newsLookupService.lookup(sourcesParams).get();
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
        resultsValueJson = newsLookupService.lookup(sourcesIdsClone).get();
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

        return asyncRequest(idParams, pagesAmount);
    }
}
