package components.extractors;
import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Result;

public class TokenAuthResponseFromResult {
    public static String jwtOf(Result result) {
        JsonNode tokenAuthResponseJson = GenericDataFromResult.jsonOf(result);
        return tokenAuthResponseJson.get("jwt").asText();
    }
}
