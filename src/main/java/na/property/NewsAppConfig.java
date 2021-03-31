package na.property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication(scanBasePackages = {"na"})
@PropertySource("classpath:newsApp.properties")
@EnableAsync
@EnableWebMvc
public class NewsAppConfig {
    private final WebApplicationContext applicationContext;

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

    @Autowired
    public NewsAppConfig(WebApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
