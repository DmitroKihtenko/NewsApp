package bean.error;

import org.apache.log4j.Logger;

import java.util.TreeSet;

public class MainErrorManager implements ErrorManager {
    private static final Logger logger =
            Logger.getLogger(MainErrorManager.class);

    private TreeSet<String> ignorableErrors;

    public MainErrorManager() {
        ignorableErrors = new TreeSet<>();
    }

    public void setIgnorableErrors(String ... errorCodes) {
        if(errorCodes == null) {
            logger.warn("Parameters list has null value");

            throw new IllegalArgumentException(
                    "Parameters list has null value"
            );
        }
        for(String code : errorCodes) {
            ignorableErrors.add(code);
        }
    }

    @Override
    public boolean isIgnorable(String errorCode) {
        return ignorableErrors.contains(errorCode);
    }
}
