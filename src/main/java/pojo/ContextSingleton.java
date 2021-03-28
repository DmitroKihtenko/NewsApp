package pojo;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ContextSingleton {
    static private ConfigurableApplicationContext instance = null;

    static public ConfigurableApplicationContext getInstance() {
        if(instance == null) {
            instance = new ClassPathXmlApplicationContext("/applicationContext.xml");
        }
        return instance;
    }
}
