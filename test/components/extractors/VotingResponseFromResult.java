package components.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Result;

public class VotingResponseFromResult {
    public static Long idOf(Result result) {
        JsonNode votingResponseJson = GenericDataFromResult.jsonOf(result);
        return votingResponseJson.get("id").asLong();
    }

    public static String networkOf(Result result) {
        JsonNode votingResponseJson = GenericDataFromResult.jsonOf(result);
        return votingResponseJson.get("network").asText();
    }

    public static String decryptionKeyOf(Result result) {
        JsonNode votingResponseJson = GenericDataFromResult.jsonOf(result);
        return votingResponseJson.get("decryptionKey").asText();
    }
}
