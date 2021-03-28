package bean.sources.newsApi;
import bean.sources.SourcesParams;
import org.apache.log4j.Logger;

public class NASourcesParams extends NAKeyParams implements SourcesParams {
    private final static Logger logger =
            Logger.getLogger(NASourcesParams.class);

    private String category;
    private String country;
    private String language;

    private void checkOption(String option) {
        if(option == null) {
            logger.error("Option parameter has null value");

            throw new IllegalArgumentException(
                    "Option parameter has null value"
            );
        }
        if(option.length() == 0) {
            logger.error("Option parameter is empty");

            throw new IllegalArgumentException(
                    "Option parameter is empty"
            );
        }
    }

    @Override
    public void setCategory(String option) {
        checkOption(option);
        if(option.equals("all")) {
            category = null;
        } else {
            category = option;
        }
        logger.info("It has been set category " + option);
    }

    @Override
    public void setCountry(String option) {
        checkOption(option);
        if(option.equals("all")) {
            country = null;
        } else {
            country = option;
        }
        logger.info("It has been set country " + option);
    }

    @Override
    public void setLanguage(String option) {
        checkOption(option);
        if(option.equals("all")) {
            language = null;
        } else {
            language = option;
        }
        logger.info("It has been set language " + option);
    }

    @Override
    public String getUrnString() {
        String paramsLine = "/sources";
        boolean alreadySet = false;

        if(category != null) {
            paramsLine += "?category=" + category;
            alreadySet = true;
        }
        if(country != null) {
            paramsLine += alreadySet ? "&" : "?";
            paramsLine += "country=" + country;
            alreadySet = true;
        }
        if(language != null) {
            paramsLine += alreadySet ? "&" : "?";
            paramsLine += "language=" + language;
            alreadySet = true;
        }
        if(apiKey != null) {
            paramsLine += alreadySet ? "&" : "?";
            paramsLine += "apiKey=" + apiKey;
        }

        return paramsLine;
    }
}
