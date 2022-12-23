package services.commissionsubs.userinfo;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class UserInfoCollectorForScaleTesting implements UserInfoCollector {
    @Override
    public CompletionStage<JsonNode> collect(String accessToken) {
        JsonNode json = Json.parse("{\"sub\": \"" + accessToken + "\", \"email\": \"" + accessToken+"\", \"email_verified\": true}");

        return completedFuture(json);
    }
}
