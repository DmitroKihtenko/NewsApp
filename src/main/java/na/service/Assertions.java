package na.service;

import org.apache.log4j.Logger;

public class Assertions {
    public static void isPositive(int value, String parameterName,
                                  Logger logger) {
        if(value <= 0) {
            logger.error(parameterName + " parameter has " +
                    "non-positive value " + value + ". Checked by " +
                    Assertions.class);

            throw new IllegalArgumentException(
                    parameterName + " parameter has non-positive " +
                            "value " + value
            );
        }
    }

    public static void isNotNull(Object object, String parameterName,
                                 Logger logger) {
        if(object == null) {
            logger.error(parameterName + " parameter has null value." +
                    " Checked by " + Assertions.class);

            throw new IllegalArgumentException(
                    parameterName + " parameter has null value"
            );
        }
    }

    public static void notEmptyString(String string,
                                      String parameterName,
                                      Logger logger) {
        if(string.length() == 0) {
            logger.error(parameterName + " string parameter is " +
                    "empty. Checked by " + Assertions.class);

            throw new IllegalArgumentException(
                    parameterName + " string parameter is empty"
            );
        }
    }
}
