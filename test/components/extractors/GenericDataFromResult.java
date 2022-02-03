package components.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Result;

import static play.test.Helpers.contentAsString;

public class GenericDataFromResult {
    public static int statusOf(Result result) {
        return result.status();
    }

    public static JsonNode jsonOf(Result result) {
        String jsonStr = contentAsString(result);
        return Json.parse(jsonStr);
    }
}
