package na.pojo;

import na.service.Assertions;
import org.apache.log4j.Logger;

import java.util.Objects;

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
        Assertions.isNotNull(errorCode, "Error code", logger);
        Assertions.isNotNull(errorMessage, "Error message", logger);

        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public void setResult(T resultObject) {
        Assertions.isNotNull(resultObject, "result object", logger);

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResultAndError<?> that = (ResultAndError<?>) o;
        return Objects.equals(logger, that.logger) &&
                Objects.equals(result, that.result) &&
                Objects.equals(errorCode, that.errorCode)
                && Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logger, result, errorCode, errorMessage);
    }
}
