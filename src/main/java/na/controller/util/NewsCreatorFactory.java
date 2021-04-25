package na.controller.util;

import na.service.Assertions;
import na.service.MediaTypeLogic;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component("creatorFactory")
public class NewsCreatorFactory {
    private final static Logger logger =
            Logger.getLogger(NewsCreatorFactory.class);
    private static final String docxMediaType =
            "application/vnd.openxmlformats-officedocument." +
            "wordprocessingml.document";
    private final ApplicationContext beanContext;

    @Autowired
    public NewsCreatorFactory(ApplicationContext beanContext) {
        Assertions.isNotNull(beanContext, "Beans context", logger);

        this.beanContext = beanContext;
    }

    public ResponseCreator getCreator(MediaType mediaType) {
        String mediaTypeString;
        if(mediaType != null) {
            mediaTypeString = mediaType.getType() + '/' +
                    mediaType.getSubtype();
            logger.info("Request media type: " + mediaTypeString);
        } else {
            logger.warn("No request media type");
            return null;
        }
        ResponseCreator resolvedCreator = null;
        String message = "Resolved response media type: ";

        switch (mediaTypeString) {
            case MediaType.APPLICATION_JSON_VALUE:
                logger.info(message +
                        MediaType.APPLICATION_JSON_VALUE);
                resolvedCreator = (ResponseCreator) beanContext.
                        getBean("jsonCreator");

                break;
            case MediaType.APPLICATION_XML_VALUE:
                logger.info(message +
                        MediaType.APPLICATION_XML_VALUE);
                resolvedCreator = (ResponseCreator) beanContext.
                        getBean("xmlCreator");

                break;
            case docxMediaType:
                logger.info(message + docxMediaType);
                resolvedCreator = (ResponseCreator) beanContext.
                        getBean("docxCreator");

                break;
            default:
                logger.info("Media type was not resolved");
        }
        return resolvedCreator;
    }

    public ResponseCreator getCreator(String responseType) {
        if(responseType != null) {
            logger.info("Request response format: " + responseType);
        } else {
            logger.warn("No request response type");
            return null;
        }

        ResponseCreator resolvedCreator = null;
        String message = "Resolved response media type: ";

        switch (responseType) {
            case "json":
                logger.info(message +
                        MediaType.APPLICATION_JSON_VALUE);
                resolvedCreator = (ResponseCreator) beanContext.
                        getBean("jsonCreator");

                break;
            case "xml":
                logger.info(message +
                        MediaType.APPLICATION_XML_VALUE);
                resolvedCreator = (ResponseCreator) beanContext.
                        getBean("xmlCreator");

                break;
            case "docx":
                logger.info(message + docxMediaType);
                resolvedCreator = (ResponseCreator) beanContext.
                        getBean("docxCreator");

                break;
            default:
                logger.info("Media type was not resolved");
        }
        return resolvedCreator;
    }
}
