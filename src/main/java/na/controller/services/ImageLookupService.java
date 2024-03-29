package na.controller.services;

import na.service.Assertions;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
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
        Assertions.isNotNull(restTemplate, "Rest template",
                logger);

        this.restTemplate = restTemplate;
    }

    @Async("mainThreadPoolTaskExecutor")
    public CompletableFuture<ResponseEntity<byte[]>> asyncLookup(String uri) {
        ResponseEntity<byte[]> response;
        HttpHeaders requestHeaders = new HttpHeaders();
        List<MediaType> requestMediaTypes = new ArrayList<>(1);
        requestMediaTypes.add(MediaType.IMAGE_JPEG);
        requestHeaders.setAccept(requestMediaTypes);
        HttpEntity<String> requestEntity = new HttpEntity<>("",
                requestHeaders);

        logger.info("Attempt to lookup response from " + uri);

        try {
            response = restTemplate.exchange(uri, HttpMethod.GET,
                    requestEntity,
                    byte[].class);
        } catch (RestClientResponseException e) {
            logger.warn("Source returned status code: " +
                    e.getRawStatusCode());

            response = ResponseEntity.status(e.getRawStatusCode()).
                    body(e.getResponseBodyAsByteArray());
        } catch (ResourceAccessException e) {
            logger.error("Image get access error: " +
                    e.getMessage());

            response = ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR.value()).
                    body(null);
        }

        logger.info("Successfully got response");

        return CompletableFuture.completedFuture(response);
    }
}
