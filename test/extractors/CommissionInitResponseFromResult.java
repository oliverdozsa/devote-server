package extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Result;

import static extractors.GenericDataFromResult.jsonOf;

public class CommissionInitResponseFromResult {
    public static String publicKeyOf(Result result) {
        JsonNode initResponse = jsonOf(result);
        return initResponse.get("publicKey").asText();
    }
}
