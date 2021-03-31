package na.sources;

import org.springframework.http.MediaType;

public interface UrnParams extends Cloneable {
    String getUrnString();
    MediaType getRequiredMediaType();
    UrnParams clone() throws CloneNotSupportedException;
}
