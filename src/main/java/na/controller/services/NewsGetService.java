package na.controller.services;

import na.parser.NewsParser;
import na.pojo.News;
import na.pojo.ResultAndError;
import na.sources.IdParams;
import na.sources.NewsSite;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class NewsGetService {
    private final static Logger logger =
            Logger.getLogger(NewsGetService.class);

    private NewsLookupService lookupService;
    private int lookupThreads;
    private final NewsParser fullParser;
    private Integer lastErrorCode;

    @Autowired
    public NewsGetService(NewsLookupService lookupService,
                          @Qualifier("newsFullParser")
                                     NewsParser fullParser) {
        if(lookupService == null) {
            logger.error("News lookup service has null value");

            throw new IllegalArgumentException(
                    "News lookup service has null value"
            );
        }
        if(fullParser == null) {
            logger.error("News full parser parameter has null value");

            throw new IllegalArgumentException(
                    "News full parser parameter has null value"
            );
        }
        this.lookupService = lookupService;
        this.fullParser = fullParser;
        lookupThreads = 1;
    }

    @Autowired
    public void setLookupThreads(@Value("${newsLookupThreads}") int lookupThreads) {
        if(lookupThreads <= 0) {
            logger.error("Lookup threads amount has " +
                    "non-positive value");

            throw new IllegalArgumentException(
                    "Lookup threads amount has non-positive value"
            );
        }
        this.lookupThreads = lookupThreads;
    }

    public Integer getLastErrorCode() {
        return lastErrorCode;
    }

    public ResultAndError<Iterable<News>>
    asyncRequest(IdParams idParams, int pagesAmount) throws ExecutionException, InterruptedException {
        LinkedList<News> generalList = new LinkedList<>();
        ResultAndError<Iterable<News>> rae =
                new ResultAndError<>(generalList);
        ResultAndError<Iterable<News>> newsJsonResult;
        ResponseEntity<String> requestResult;
        CompletableFuture<ResponseEntity<String>>[] threadResults =
                new CompletableFuture[lookupThreads];
        int threadIter = 0;
        IdParams cloneIdParams;

        logger.info("Starting send asynchronous news requests " +
                "with " + lookupThreads + " threads");

        for(int page = 1; page <= pagesAmount; page++) {
            cloneIdParams = idParams.clone();
            cloneIdParams.setPage(page);
            threadResults[threadIter] =
                    lookupService.asyncLookup(cloneIdParams);

            if(threadIter >= lookupThreads - 1 ||
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
                            fullParser.parse(requestResult.getBody());

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
}
