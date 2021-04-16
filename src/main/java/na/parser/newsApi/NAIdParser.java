package na.parser.newsApi;

import na.parser.NewsParser;
import org.springframework.stereotype.Component;
import na.pojo.ResultAndError;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.LinkedList;
import java.util.List;

@Component("sourcesIdParser")
public class NAIdParser implements NewsParser {
    private static final Logger logger =
            Logger.getLogger(NAIdParser.class);

    @Override
    public ResultAndError<List<String>> parse(String jsonString) {
        JSONObject jsonObject;
        JSONArray newsArray;
        LinkedList<String> returnList = new LinkedList<>();
        ResultAndError<List<String>> resultIds = new
                ResultAndError<>(returnList);
        int idErrors = 0;

        logger.info("Starting parsing news sources");

        try {
            jsonObject = new JSONObject(jsonString);

            String status = (String) jsonObject.get("status");

            if(!status.equals("ok")) {
                resultIds.setError((String) jsonObject.get("code"),
                        (String) jsonObject.get("message"));
            } else {
                newsArray = (JSONArray) jsonObject.get("sources");
                for(int counter = 0; counter < newsArray.length();
                    counter++) {
                    try {
                        returnList.add((String)((JSONObject)
                                newsArray.get(counter)).get("id"));
                    } catch (JSONException | ClassCastException e) {
                        idErrors++;
                    }
                }
            }
        } catch (JSONException | ClassCastException e) {
            logger.error("Parse error" + e.getMessage());

            resultIds.setError("parseError",
                    e.getMessage());
        }
        if(idErrors != 0) {
            logger.error("There are " + idErrors +
                    "id parsing errors");

            resultIds.setError("SourceIdParseError",
                    "There are " + idErrors +
                            "id parsing errors");
        } else {
            logger.info("Successfully parsed");
        }

        return resultIds;
    }
}
