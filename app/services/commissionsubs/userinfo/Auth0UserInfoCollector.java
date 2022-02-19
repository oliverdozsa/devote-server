package services.commissionsubs.userinfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class Auth0UserInfoCollector implements UserInfoCollector {
    private final String userInfoUrl;
    private final WSClient ws;

    @Inject
    public Auth0UserInfoCollector(Config config, WSClient ws) {
        this.ws = ws;
        userInfoUrl = config.getString("devote.jwt.issuer") + "userinfo";
    }

    @Override
    public CompletionStage<JsonNode> collect(String accessToken) {
        WSRequest request = ws.url(userInfoUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + accessToken);

        return request.get()
                .thenApply(r -> r.getBody(WSBodyReadables.instance.json()));
    }
}
