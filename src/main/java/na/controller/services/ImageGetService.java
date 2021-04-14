package na.controller.services;

import na.pojo.News;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ImageGetService {
    private final static Logger logger =
            Logger.getLogger(ImageGetService.class);

    private final ImageLookupService lookupService;
    private int lookupThreads;

    @Autowired
    public ImageGetService(ImageLookupService lookupService) {
        if(lookupService == null) {
            logger.error("Images lookup service has null value");

            throw new IllegalArgumentException(
                    "Images lookup service has null value"
            );
        }
        this.lookupService = lookupService;
    }

    @Autowired
    public void setLookupThreads(@Value("${imagesLookupThreads}") int lookupThreads) {
        if(lookupThreads <= 0) {
            logger.error("Image lookup threads has " +
                    "non-positive value");

            throw new IllegalArgumentException(
                    "Image lookup threads has non-positive value"
            );
        }
        this.lookupThreads = lookupThreads;
    }

    public Map<Integer, ByteArrayOutputStream>
    asyncRequests(Iterable<News> newsList) throws IOException, ExecutionException, InterruptedException {
        TreeMap<Integer, ByteArrayOutputStream> resultMap =
                new TreeMap<>();
        ByteArrayOutputStream imageStream;
        CompletableFuture<ResponseEntity<byte[]>>[]
                imageResponses = new
                CompletableFuture[lookupThreads];
        ResponseEntity<byte[]> lookupResult;
        int newsIndex = 0;
        int insertIndex = 0;
        int threadIter = 0;

        Iterator<News> newsIter = newsList.iterator();
        News news;

        logger.info("Starting send asynchronous image get requests " +
                "with " + lookupThreads + " threads");

        while(newsIter.hasNext()) {
            newsIndex++;
            news = newsIter.next();
            if(news.getImageUrl() != null) {
                resultMap.put(newsIndex, null);
                try {
                    imageResponses[threadIter] =
                            lookupService.asyncLookup(news.
                                    getImageUrl());
                } catch (Exception e) {
                    logger.warn("Error while downloading image " +
                            "file from " + news.getImageUrl() +
                            " .Exception message: " +
                            Objects.requireNonNullElse(e.getMessage(),
                                    e.toString()));
                }

                if (threadIter >= lookupThreads - 1 ||
                        !newsIter.hasNext()) {

                    if (threadIter < lookupThreads - 1) {
                        CompletableFuture.allOf(Arrays.
                                copyOf(imageResponses,
                                        threadIter + 1)).join();
                    } else {
                        CompletableFuture.allOf(imageResponses).join();
                    }

                    for (int threadRead = 0; threadRead <= threadIter;
                         threadRead++) {
                        insertIndex++;
                        while(!resultMap.containsKey(
                                insertIndex) && insertIndex
                                <= newsIndex) {
                            insertIndex++;
                        }

                        try {
                            lookupResult =
                                    imageResponses[threadRead].get();
                            if (lookupResult.getStatusCode().value() <
                                    HttpStatus.BAD_REQUEST.value()) {
                                imageStream = new
                                        ByteArrayOutputStream();
                                imageStream.write(lookupResult.
                                        getBody());

                                resultMap.put(insertIndex, imageStream);
                            } else {
                                logger.warn("News image source " +
                                        "returned status code " +
                                        lookupResult.getStatusCode().
                                                value());

                                resultMap.remove(insertIndex);
                            }
                        } catch (NullPointerException e) {
                            logger.warn("NullPointerException when " +
                                    "trying to get image stream");
                        }
                    }

                    threadIter = 0;
                } else {
                    threadIter++;
                }
            }
        }

        logger.info("Image get completed successfully");

        return resultMap;
    }
}
