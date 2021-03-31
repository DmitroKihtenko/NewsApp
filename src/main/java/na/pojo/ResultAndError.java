package na.pojo;

import org.apache.log4j.Logger;

public class ResultAndError<T> {
    private final Logger logger = Logger.getLogger(ResultAndError.class);

    private T result;
    private String errorCode;
    private String errorMessage;

    public ResultAndError(String errorCode, String errorMessage) {
        setError(errorCode, errorMessage);
    }

    public ResultAndError(T resultObject) {
        setResult(resultObject);
    }

    public void setError(String errorCode, String errorMessage) {
        if(errorCode == null || errorMessage == null) {
            logger.error("Error parameter has null value");

            throw new IllegalArgumentException(
                    "Error parameter has null value"
            );
        }
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public void setResult(T resultObject) {
        if (resultObject == null) {
            logger.error("Result parameter has null value");

            throw new IllegalArgumentException("Result parameter has null value");
        }
        result = resultObject;
    }

    public T getResult() {
        return result;
    }

    public boolean getStatus() {
        return errorMessage == null;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "ResultAndError{" +
                "result=" + result +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
