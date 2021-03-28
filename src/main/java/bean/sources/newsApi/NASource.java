package bean.sources.newsApi;

import bean.sources.NewsSource;
import bean.sources.UrnParams;
import org.apache.http.HttpResponse;
import pojo.ResultAndError;
import service.Http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class NASource implements NewsSource {
    private final static String URL;

    private Integer lastHttpStatus;

    static {
        URL = "https://newsapi.org/v2";
    }

    @Override
    public Integer getLastHttpStatus() {
        return lastHttpStatus;
    }

    @Override
    public ResultAndError<String> getRawResponse(
            UrnParams params, int connectionTimeout) {
        String responseBody = "";
        ResultAndError<String> rae = new ResultAndError<>(responseBody);

        try {
            HttpResponse response = Http.getResponse(URL +
                            params.getUrnString(),
                    connectionTimeout, "utf-8");
            lastHttpStatus = response.getStatusLine().getStatusCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(response.
                            getEntity().getContent()));
            StringBuilder content = new StringBuilder();
            String inputLine = in.readLine();
            while (inputLine != null) {
                content.append(inputLine);
                inputLine = in.readLine();
            }
            in.close();
            responseBody = content.toString();
            rae.setResult(responseBody);

        } catch (IOException e) {
            rae.setError("NewsSourceConnectionError",
                    e.getMessage());
        }

        return rae;
    }
}
