package service;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class Http {
    public static HttpResponse getResponse(String uri, int connectionTimeout, String charset) throws IOException {
        HttpGet httpGet = new HttpGet(uri);
        if(charset != null) {
            httpGet.setHeader("Content-Type",
                    "application/json; charset=" + charset);
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig config = RequestConfig.custom().
                setConnectTimeout(connectionTimeout).
                setConnectionRequestTimeout(connectionTimeout).
                setSocketTimeout(connectionTimeout).
                build();

        httpGet.setConfig(config);

        return httpClient.execute(httpGet);
    }

    public static HttpResponse getResponse(String uri, int connectionTimeout) throws IOException {
        return getResponse(uri, connectionTimeout, null);
    }
}
