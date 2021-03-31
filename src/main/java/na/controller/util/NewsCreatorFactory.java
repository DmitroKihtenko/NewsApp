package na.controller.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component("creatorFactory")
public class NewsCreatorFactory {
    private ApplicationContext beanContext;

    @Autowired
    public void setBeanContext(ApplicationContext beanContext) {
        this.beanContext = beanContext;
    }

    public ResponseCreator getCreator(MediaType mediaType) {
        switch (mediaType.getType() + '/' + mediaType.getSubtype()) {
            case MediaType.APPLICATION_JSON_VALUE:
                return (ResponseCreator) beanContext.
                        getBean("jsonCreator");

            case MediaType.APPLICATION_XML_VALUE:
                return (ResponseCreator) beanContext.
                        getBean("xmlCreator");

            case "application/vnd.openxmlformats-officedocument." +
                    "wordprocessingml.document":
                return (ResponseCreator) beanContext.
                        getBean("docxCreator");

            default:
                return null;
        }
    }
}
