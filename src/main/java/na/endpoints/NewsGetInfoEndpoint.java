package na.endpoints;

import na.controller.services.ImageGetService;
import na.controller.services.NewsGetService;
import na.service.Assertions;
import na.sources.IdParams;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Endpoint(id = "newsGetInfo")
public class NewsGetInfoEndpoint {
    private final static Logger logger =
            Logger.getLogger((NewsGetInfoEndpoint.class));
    private final Map<String, String> getNewsInfo;

    public NewsGetInfoEndpoint() {
        getNewsInfo = new ConcurrentHashMap<>(4);
        getNewsInfo.put("pageSize", "No info");
        getNewsInfo.put("maximalResponseAmount", "No info");
        getNewsInfo.put("newsGetThreads", "No info");
        getNewsInfo.put("imageGetThreads", "No info");
    }

    @Autowired
    public void setPageSize(IdParams idParams) {
        Assertions.isNotNull(idParams, "News id params object",
                logger);

        getNewsInfo.put("pageSize", idParams.getPageSize().toString());
    }

    @Autowired
    public void setResponseAmount(NewsGetService newsGetService) {
        Assertions.isNotNull(newsGetService, "News get service",
                logger);

        getNewsInfo.put("maximalResponseAmount", String.valueOf(
                newsGetService.getMaximalNewsCount()));
    }

    @Autowired
    public void setNewsGetThreads(NewsGetService newsGetService) {
        Assertions.isNotNull(newsGetService, "News get service",
                logger);

        getNewsInfo.put("newsGetThreads", String.valueOf(
                newsGetService.getLookupThreads()));
    }

    @Autowired
    public void setImageGetThreads(ImageGetService imageGetService) {
        Assertions.isNotNull(imageGetService, "Image get service",
                logger);

        getNewsInfo.put("imageGetThreads", String.valueOf(
                imageGetService.getLookupThreads()));
    }

    @ReadOperation
    public Map<String, String> getNewsGetInfo() {
        return getNewsInfo;
    }
}
