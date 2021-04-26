package na.parser.newsApi;

import na.parser.NewsParser;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import na.pojo.ResultAndError;

import java.util.Objects;

@Component("resultsValueParser")
public class NAResultsParser implements NewsParser {
    private static final Logger logger = Logger.getLogger(NAResultsParser.class);

    @Override
    public ResultAndError<Integer> parse(String jsonString) {
        JSONObject jsonObject;
        ResultAndError<Integer> newsResults =
                new ResultAndError<>(0);

        logger.info("Starting parsing results amount");

        try {
            jsonObject = new JSONObject(jsonString);

            String status = (String) jsonObject.get("status");

            if (!status.equals("ok")) {
                newsResults.setError((String) jsonObject.
                        get("code"), (String) jsonObject.
                        get("message"));

                logger.error("News source error: " +
                        newsResults.getErrorMessage());

                return newsResults;
            }

            newsResults.setResult((Integer) jsonObject.
                    get("totalResults"));

        } catch (JSONException | ClassCastException e) {
            logger.error("Results parse error: " + Objects.
                    requireNonNullElse(e.getMessage(), e.toString()));

            newsResults.setError("parseError", Objects.
                    requireNonNullElse(e.getMessage(), e.toString()));
        }

        logger.info("Successfully parsed");

        return newsResults;
    }
}
