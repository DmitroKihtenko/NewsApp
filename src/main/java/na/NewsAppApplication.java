package na;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@SpringBootApplication(scanBasePackages = {"na"})
@EnableAsync
@EnableWebMvc
public class NewsAppApplication {
    @Bean("mainThreadPoolTaskExecutor")
    public TaskExecutor getAsyncExecutor(
            @Value("${runtimeThreads}") int runtimeThreads) {
        if(runtimeThreads <= 0) {
            throw new IllegalArgumentException(
                    "Runtime threads parameter has non-positive value"
            );
        }
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(runtimeThreads);
        taskExecutor.setMaxPoolSize(runtimeThreads);
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setThreadNamePrefix("Async-");
        return taskExecutor;
    }

    @Bean("customRestTemplate")
    @Scope("prototype")
    public RestTemplate getCustomRestTemplate()
            throws KeyStoreException, NoSuchAlgorithmException,
            KeyManagementException {
        TrustStrategy acceptingTrustStrategy =
                (x509Certificates, s) -> true;

        SSLContext sslContext = SSLContexts.custom().
                loadTrustMaterial(
                null, acceptingTrustStrategy
                ).build();
        SSLConnectionSocketFactory csf =
                new SSLConnectionSocketFactory(
                sslContext, new NoopHostnameVerifier()
        );
        CloseableHttpClient httpClient = HttpClients.custom().
                setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        return new RestTemplate(requestFactory);
    }

    public static void main(String[] args) {
        SpringApplication.run(NewsAppApplication.class, args);
    }
}
