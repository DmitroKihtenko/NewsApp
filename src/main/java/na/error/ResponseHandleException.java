package na.error;

import na.pojo.ResultAndError;

import java.util.Objects;

public class ResponseHandleException extends Exception {
    private final String errorCode;

    public ResponseHandleException(String errorCode, String errorMessage) {
        super(Objects.requireNonNullElse(errorMessage,
                "Unknown error"));
        this.errorCode = Objects.requireNonNullElse(errorCode,
                "Unknown error");
    }

    public ResponseHandleException(ResultAndError<?> rae) {
        super(Objects.requireNonNullElse(rae.getErrorMessage(),
                "Unknown error"));
        this.errorCode = Objects.requireNonNullElse(rae.getErrorCode(),
                "Unknown error");
    }

    @Override
    public String getMessage() {
        return "Error code: " + errorCode + ". Error message: " +
                super.getMessage();
    }

    @Override
    public String toString() {
        return "ResponseHandleException{" +
                "errorCode='" + errorCode + '\'' +
                "errorMessage='" + super.getMessage() + '\'' +
                '}';
    }
}
