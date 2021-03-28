package bean.parser;

import pojo.ResultAndError;

public interface NewsParser {
    ResultAndError<?> parse(String jsonString);
}
