package service;

import org.apache.log4j.Logger;
import org.springframework.http.MediaType;

public class MediaTypeLogic {
    private static final Logger logger = Logger.getLogger(MediaTypeLogic.class);

    public static MediaType createFromString(String mediaType) {
        if(mediaType == null) {
            logger.error("Media type string has null value");

            throw new IllegalArgumentException(
                    "Media type string has null value"
            );
        }

        String type = null;
        String subtype = null;

        for(int counter = 0; counter < mediaType.length();
            counter++) {
            if(mediaType.charAt(counter) == '/' ||
                    mediaType.charAt(counter) == '\\') {
                type = mediaType.substring(0, counter);
                subtype = mediaType.substring(counter + 1);

                break;
            }
        }

        if(type == null || type.length() == 0
                || subtype.length() == 0) {
            logger.error("Media type string has invalid format");

            throw new IllegalArgumentException(
                    "Media type string has invalid format"
            );
        }

        return new MediaType(type, subtype);
    }
}
