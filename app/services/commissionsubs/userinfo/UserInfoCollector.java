package services.commissionsubs.userinfo;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.concurrent.CompletionStage;

public interface UserInfoCollector {
    CompletionStage<JsonNode> collect(String accessToken);
}
