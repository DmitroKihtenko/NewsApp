package na.controller.services;

import na.parser.NewsParser;
import na.pojo.News;
import na.pojo.ResultAndError;
import na.sources.IdParams;
import na.sources.NewsSite;
import na.sources.UrnParams;
import org.apache.commons.codec.Charsets;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class NewsLookupService {
    private static final Logger logger =
            Logger.getLogger(NewsLookupService.class);

    private final RestTemplate restTemplate;
    private final NewsSite newsSite;
    private int lookupThreads;
    private final NewsParser fullParser;
    private Integer lastErrorCode;

    @Autowired
    public NewsLookupService(NewsSite newsSite,
                             @Qualifier("newsFullParser")
                                     NewsParser fullParser) {
        if(newsSite == null) {
            logger.error("News source has null value");

            throw new IllegalArgumentException(
                    "News source has null value"
            );
        }
        if(fullParser == null) {
            logger.error("News full parser parameter has null value");

            throw new IllegalArgumentException(
                    "News full parser parameter has null value"
            );
        }
        this.newsSite = newsSite;
        this.fullParser = fullParser;
        lookupThreads = 1;
        restTemplate = new RestTemplate();
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

    @Async("mainThreadPoolTaskExecutor")
    public CompletableFuture<ResponseEntity<String>> asyncLookup(
            UrnParams urnParams) {
        return CompletableFuture.completedFuture(lookup(urnParams));
    }

    public ResponseEntity<String> lookup(UrnParams urnParams) {
        ResponseEntity<String> response;
        String requestUri = newsSite.getFullUri(urnParams);
        HttpHeaders requestHeaders = new HttpHeaders();
        List<MediaType> requestMediaTypes = new ArrayList<>(1);
        requestMediaTypes.add(urnParams.getRequiredMediaType());
        requestHeaders.setAccept(requestMediaTypes);
        List<Charset> requestCharset = new ArrayList<>(1);
        requestCharset.add(Charsets.toCharset("UTF-8"));
        requestHeaders.setAcceptCharset(requestCharset);
        HttpEntity<String> requestEntity = new HttpEntity<>("", requestHeaders);

        logger.info("Attempt to lookup response from " + requestUri);

        try {
            response = restTemplate.exchange(requestUri, HttpMethod.GET,
                    requestEntity, String.class);
        } catch (RestClientResponseException e) {
            logger.info("Source returned status code: " + e.getRawStatusCode());

            response = ResponseEntity.status(e.getRawStatusCode()).
                    body(e.getResponseBodyAsString());
        }

        logger.info("Successfully got response");

        return response;
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
            threadResults[threadIter] = asyncLookup(cloneIdParams);

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
