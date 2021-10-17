package extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Result;

import static extractors.GenericDataFromResult.jsonOf;

public class VotingResponseFromResult {
    public static Long idOf(Result result) {
        JsonNode votingResponseJson = jsonOf(result);
        return votingResponseJson.get("id").asLong();
    }

    public static String networkOf(Result result) {
        JsonNode votingResponseJson = jsonOf(result);
        return votingResponseJson.get("network").asText();
    }
}
