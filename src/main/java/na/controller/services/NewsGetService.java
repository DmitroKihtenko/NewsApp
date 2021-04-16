package na.controller.services;

import na.parser.NewsParser;
import na.pojo.News;
import na.pojo.ResultAndError;
import na.service.Assertions;
import na.sources.IdParams;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class NewsGetService {
    private final static Logger logger =
            Logger.getLogger(NewsGetService.class);

    private final NewsLookupService lookupService;
    private int lookupThreads;
    private final NewsParser fullParser;
    private Integer lastErrorCode;

    @Autowired
    public NewsGetService(NewsLookupService lookupService,
                          @Qualifier("newsFullParser")
                                     NewsParser fullParser) {
        Assertions.isNotNull(lookupService, "News lookup service",
                logger);
        Assertions.isNotNull(fullParser, "News full parser",
                logger);
        this.lookupService = lookupService;
        this.fullParser = fullParser;
        lookupThreads = 1;
    }

    @Autowired
    public void setLookupThreads(@Value("${newsLookupThreads}") int lookupThreads) {
        Assertions.isPositive(lookupThreads, "News lookup threads",
                logger);

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
