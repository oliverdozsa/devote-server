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

    public static Boolean isOnTestNetworkOf(Result result) {
        JsonNode votingResponseJson = GenericDataFromResult.jsonOf(result);
        return votingResponseJson.get("isOnTestNetwork").asBoolean();
    }

    public static String titleOf(Result result) {
        JsonNode votingResponseJson = GenericDataFromResult.jsonOf(result);
        return votingResponseJson.get("title").asText();
    }

    public static String ballotTypeOf(Result result) {
        JsonNode votingResponseJson = GenericDataFromResult.jsonOf(result);
        return votingResponseJson.get("ballotType").asText();
    }
    public static Integer maxChoicesOf(Result result) {
        JsonNode votingResponseJson = GenericDataFromResult.jsonOf(result);
        return votingResponseJson.get("maxChoices").asInt();
    }
}
