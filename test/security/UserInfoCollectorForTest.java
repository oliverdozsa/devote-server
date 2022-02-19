package security;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import services.commissionsubs.userinfo.UserInfoCollector;

import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class UserInfoCollectorForTest implements UserInfoCollector {
    private static JsonNode returnValue;

    public static void setReturnValue(JsonNode value) {
        returnValue = value;
    }

    public static void setReturnValueFor(String userId) {
        UserInfoCollectorForTest.setReturnValue(Json.parse("{\"sub\": \"" + userId + "\", \"email\": \"" + userId.toLowerCase() + "@mail.com\", \"email_verified\": true}"));
    }

    @Override
    public CompletionStage<JsonNode> collect(String accessToken) {
        return completedFuture(returnValue);
    }
}
