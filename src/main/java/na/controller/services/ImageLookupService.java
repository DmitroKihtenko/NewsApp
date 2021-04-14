package na.controller.services;

import na.pojo.News;
import na.service.MediaTypeLogic;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ImageLookupService {
    private static final Logger logger =
            Logger.getLogger(ImageLookupService.class);

    private final RestTemplate restTemplate;
    private int lookupThreads;

    public ImageLookupService() {
        restTemplate = new RestTemplate();
        lookupThreads = 1;
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

    @Async("mainThreadPoolTaskExecutor")
    public CompletableFuture<ResponseEntity<byte[]>> asyncLookup(String uri) {
        ResponseEntity<byte[]> response;
        HttpHeaders requestHeaders = new HttpHeaders();
        List<MediaType> requestMediaTypes = new ArrayList<>(1);
        requestMediaTypes.add(MediaTypeLogic.createFromString(MediaType.IMAGE_JPEG_VALUE));
        requestHeaders.setAccept(requestMediaTypes);
        HttpEntity<String> requestEntity = new HttpEntity<>("", requestHeaders);

        logger.info("Attempt to lookup response from " + uri);

        try {
            response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity,
                    byte[].class);
        } catch (RestClientResponseException e) {
            logger.info("Source returned status code: " + e.getRawStatusCode());

            response = ResponseEntity.status(e.getRawStatusCode()).
                    body(e.getResponseBodyAsByteArray());
        }

        logger.info("Successfully got response");

        return CompletableFuture.completedFuture(response);
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
                            asyncLookup(news.getImageUrl());
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
