package components.extractors.voting;
import com.fasterxml.jackson.databind.JsonNode;
import components.extractors.GenericDataFromResult;
import play.mvc.Result;

public class TokenAuthResponseFromResult {
    public static String jwtOf(Result result) {
        JsonNode tokenAuthResponseJson = GenericDataFromResult.jsonOf(result);
        return tokenAuthResponseJson.get("token").asText();
    }
}
