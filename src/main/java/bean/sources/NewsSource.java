package bean.sources;

import pojo.ResultAndError;

public interface NewsSource {
    Integer getLastHttpStatus();
    ResultAndError<String> getRawResponse(UrnParams params, int connectionTimeout);
}
