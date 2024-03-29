package na.controller.services;

import na.service.Assertions;
import na.sources.NewsSite;
import na.sources.UrnParams;
import org.apache.commons.codec.Charsets;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class NewsLookupService {
    private static final Logger logger =
            Logger.getLogger(NewsLookupService.class);

    private final RestTemplate restTemplate;
    private final NewsSite newsSite;

    @Autowired
    public NewsLookupService(RestTemplate restTemplate,
                             NewsSite newsSite) {
        Assertions.isNotNull(restTemplate, "Rest template", logger);
        Assertions.isNotNull(newsSite, "News site", logger);

        this.newsSite = newsSite;
        this.restTemplate = restTemplate;
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
        HttpEntity<String> requestEntity = new HttpEntity<>("",
                requestHeaders);

        logger.info("Attempt to lookup response from " + requestUri);

        try {
            response = restTemplate.exchange(requestUri,
                    HttpMethod.GET, requestEntity, String.class);
        } catch (RestClientResponseException e) {
            logger.info("Source returned status code: " +
                    e.getRawStatusCode());

            response = ResponseEntity.status(e.getRawStatusCode()).
                    body(e.getResponseBodyAsString());
        }

        logger.info("Successfully got response");

        return response;
    }
}
