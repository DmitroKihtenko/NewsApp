package na.controller.services;

import na.service.MediaTypeLogic;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class ImageLookupService {
    private static final Logger logger =
            Logger.getLogger(ImageLookupService.class);

    private final RestTemplate restTemplate;

    @Autowired
    public ImageLookupService(RestTemplate restTemplate) {
        if(restTemplate == null) {
            logger.error("Rest template parameter has null value");

            throw new IllegalArgumentException(
                    "Rest template parameter has null value"
            );
        }
        this.restTemplate = restTemplate;
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
}
