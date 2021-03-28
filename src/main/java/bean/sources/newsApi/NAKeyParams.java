package bean.sources.newsApi;

import bean.sources.UrnParams;
import org.apache.log4j.Logger;

public abstract class NAKeyParams implements UrnParams {
    private static final Logger logger = Logger.getLogger(NAKeyParams.class);

    protected String apiKey;

    public void setApiKey(String apiKey) {
        if(apiKey == null) {
            logger.error("API key parameter has null value");

            throw new IllegalArgumentException(
                    "API key parameter has null value"
            );
        }
        this.apiKey = apiKey;
    }
}
