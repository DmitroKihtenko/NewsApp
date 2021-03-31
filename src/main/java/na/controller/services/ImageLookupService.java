package na.controller.services;

import na.pojo.MediaTypeLogic;
import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ImageLookupService {
    private static final Logger logger =
            Logger.getLogger(ImageLookupService.class);

    private final RestTemplate restTemplate;

    public ImageLookupService() {
        restTemplate = new RestTemplate();
    }

    @Async("mainThreadPoolTaskExecutor")
    public CompletableFuture<ResponseEntity<byte[]>> lookup(String uri) {
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
