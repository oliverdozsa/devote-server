package components.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;

import static components.extractors.GenericDataFromResult.jsonOf;

public class VotingPagingItemResponsesFromResult {
    public static long totalCountOf(Result result) {
        JsonNode resultJson = jsonOf(result);
        return resultJson.get("totalCount").asLong();
    }

    public static List<String> votingIdsOf(Result result) {
        JsonNode resultJson = jsonOf(result);
        List<String> ids = new ArrayList<>();
        resultJson.get("items").forEach(item -> ids.add(item.get("id").asText()));

        return ids;
    }

    public static List<String> votingTitlesOf(Result result) {
        JsonNode resultJson = jsonOf(result);
        List<String> titles = new ArrayList<>();
        resultJson.get("items").forEach(item -> titles.add(item.get("title").asText()));

        return titles;
    }

    public static List<String> endDatesOf(Result result) {
        JsonNode resultJson = jsonOf(result);
        List<String> endDates = new ArrayList<>();
        resultJson.get("items").forEach(item -> endDates.add(item.get("endDate").asText()));

        return endDates;
    }
}
