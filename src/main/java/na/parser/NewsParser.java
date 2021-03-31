package na.parser;

import na.pojo.ResultAndError;

public interface NewsParser {
    ResultAndError<?> parse(String jsonString);
}
