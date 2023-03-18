package components.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Result;
import services.Base62Conversions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public static List<Long> votingIdsDecodedOf(Result result) {
        JsonNode resultJson = jsonOf(result);
        List<String> ids = new ArrayList<>();
        resultJson.get("items").forEach(item -> ids.add(item.get("id").asText()));

        return ids.stream().map(Base62Conversions::decode)
                .collect(Collectors.toList());
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
