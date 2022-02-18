package services.commissionsubs.userinfo;

import com.fasterxml.jackson.databind.JsonNode;

public interface UserInfoCollector {
    JsonNode collect(String accessToken);
}
