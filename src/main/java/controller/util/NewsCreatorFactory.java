package controller.util;

import org.springframework.http.MediaType;

public class NewsCreatorFactory {
    public static ResponseCreator getCreator(MediaType mediaType) {
        switch (mediaType.getType() + '/' + mediaType.getSubtype()) {
            case MediaType.APPLICATION_JSON_VALUE:
                return new JsonCreator();

            case MediaType.APPLICATION_XML_VALUE:
                return new XmlCreator();

            case "application/vnd.openxmlformats-officedocument." +
                    "wordprocessingml.document":
                return new DocxCreator();

            default:
                return null;
        }
    }
}
