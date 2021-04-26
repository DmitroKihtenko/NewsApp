package na.error;

import na.service.Assertions;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.TreeSet;

@Component
public class MainErrorManager implements ErrorManager {
    private static final Logger logger =
            Logger.getLogger(MainErrorManager.class);

    private final TreeSet<String> ignorableErrors;

    public MainErrorManager() {
        ignorableErrors = new TreeSet<>();
    }

    @Autowired
    public void setIgnorableErrors(@Value("${newsApiIgnorableErrors}")
                                               String ... errorCodes) {
        Assertions.isNotNull(errorCodes, "List of ignorable errors",
                logger);

        ignorableErrors.addAll(Arrays.asList(errorCodes));
    }

    @Override
    public boolean isIgnorable(String errorCode) {
        return ignorableErrors.contains(errorCode);
    }
}
