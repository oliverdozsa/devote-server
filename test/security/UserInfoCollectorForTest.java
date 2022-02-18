package security;

import com.fasterxml.jackson.databind.JsonNode;
import services.commissionsubs.userinfo.UserInfoCollector;

public class UserInfoCollectorForTest implements UserInfoCollector {
    private static JsonNode returnValue;

    public static void setReturnValue(JsonNode value) {
        returnValue = value;
    }

    @Override
    public JsonNode collect(String accessToken) {
        return returnValue;
    }
}
