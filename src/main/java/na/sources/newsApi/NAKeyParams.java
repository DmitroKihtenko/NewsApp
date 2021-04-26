package na.sources.newsApi;

import na.service.Assertions;
import na.sources.UrnParams;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public abstract class NAKeyParams implements UrnParams {
    private static final Logger logger = Logger.getLogger(NAKeyParams.class);

    protected String apiKey;

    @Autowired
    public void setApiKey(@Value("${newsApiKey}") String apiKey) {
        Assertions.isNotNull(apiKey, "News api key", logger);

        this.apiKey = apiKey;
    }

    @Override
    public NAKeyParams clone() throws CloneNotSupportedException {
        return (NAKeyParams) super.clone();
    }
}
