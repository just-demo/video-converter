package self.ed.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UncheckedIOException;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

public class JsonUtils {
    public static String toJson(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(INDENT_OUTPUT);
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
