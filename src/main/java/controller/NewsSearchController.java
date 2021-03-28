package controller;

import bean.parser.newsApi.NAFullParser;
import bean.parser.newsApi.NAResultsParser;
import bean.sources.IdParams;
import bean.sources.SourcesParams;
import bean.parser.newsApi.NAIdParser;
import bean.sources.NewsSource;
import pojo.ContextSingleton;
import pojo.ResultAndError;
import org.apache.log4j.Logger;
import pojo.News;

import java.util.LinkedList;
import java.util.List;

public abstract class NewsSearchController {
    private static final Logger logger =
            Logger.getLogger(NewsSearchController.class);

    private int connectionTimeout;
    protected NewsSource newsSource;
    protected int lastErrorCode;

    NewsSearchController() {
        connectionTimeout = 3000;
        lastErrorCode = 500;
    }

    public void setNewsSource(NewsSource source) {
        if(source == null) {
            logger.error("News source parameter has null value");

            throw new IllegalArgumentException(
                    "News source parameter has null value"
            );
        }
        this.newsSource = source;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        if(connectionTimeout <= 0) {
            logger.error("Connection timeout parameter has " +
                    "non-positive value");

            throw new IllegalArgumentException(
                    "Connection timeout parameter has " +
                            "non-positive value"
            );
        }
        this.connectionTimeout = connectionTimeout;
    }

    public ResultAndError<Iterable<News>> getNewsList(SourcesParams sourcesParams) {
        IdParams idParams = (IdParams)
                ContextSingleton.getInstance().
                        getBean("NAIdParams");

        NAIdParser idParser = new NAIdParser();
        NAResultsParser resultsParser = new NAResultsParser();
        NAFullParser fullParser = new NAFullParser();

        Integer pagesAmount;
        LinkedList<News> newsList = new LinkedList<>();
        ResultAndError<Iterable<News>> newsResult = new ResultAndError<>(newsList);
        fullParser.setDefaultNewsList(newsList);
        ResultAndError<Integer> pagesResult;
        int savedSize;

        ResultAndError<String> responseBody;
        List<String> sourcesIds;
        ResultAndError<List<String>> idResult;
        ResultAndError<LinkedList<News>> fullResult;

        responseBody = newsSource.getRawResponse(sourcesParams,
                connectionTimeout);
        if(newsSource.getLastHttpStatus() >= 400) {
            this.lastErrorCode = newsSource.getLastHttpStatus();
        }

        if(!responseBody.getStatus()) {
            newsResult.setError(responseBody.getErrorCode(),
                    responseBody.getErrorMessage());
            return newsResult;
        }

        idResult = idParser.parse(responseBody.getResult());
        if(!idResult.getStatus()) {
            newsResult.setError(idResult.getErrorCode(),
                    idResult.getErrorMessage());
            return newsResult;
        }

        if(!idResult.getResult().isEmpty()) {
            sourcesIds = idResult.getResult();
            idParams.setIdsList(sourcesIds);

            savedSize = idParams.getPageSize();
            idParams.setPageSize(1);
            responseBody = newsSource.getRawResponse(idParams,
                    connectionTimeout);
            if(newsSource.getLastHttpStatus() >= 400) {
                this.lastErrorCode = newsSource.getLastHttpStatus();
            }
            if(!responseBody.getStatus()) {
                newsResult.setError(responseBody.getErrorCode(),
                        responseBody.getErrorMessage());
                return newsResult;
            }
            idParams.setPageSize(savedSize);

            pagesResult = resultsParser.parse(responseBody.getResult());
            if(!pagesResult.getStatus()) {
                newsResult.setError(pagesResult.getErrorCode(),
                        pagesResult.getErrorMessage());
                return newsResult;
            }

            pagesAmount = pagesResult.getResult() / idParams.getPageSize();
            if (sourcesIds.size() % idParams.getPageSize() != 0) {
                pagesAmount++;
            }

            for (int page = 1; page <= pagesAmount; page++) {
                idParams.setPage(page);

                responseBody = newsSource.getRawResponse(idParams,
                        connectionTimeout);
                if(newsSource.getLastHttpStatus() >= 400) {
                    this.lastErrorCode = newsSource.getLastHttpStatus();
                }
                if(!responseBody.getStatus()) {
                    newsResult.setError(responseBody.getErrorCode(),
                            responseBody.getErrorMessage());
                    return newsResult;
                }

                fullResult = fullParser.parse(responseBody.getResult());
                if(!fullResult.getStatus()) {
                    newsResult.setError(fullResult.getErrorCode(),
                            fullResult.getErrorMessage());
                    return newsResult;
                }
            }
        }

        return newsResult;
    }
}
