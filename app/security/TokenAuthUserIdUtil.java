package security;

import com.typesafe.config.Config;

import javax.inject.Inject;
import java.util.UUID;

public class TokenAuthUserIdUtil {
    private final String tokenAuthSubjectPrefix;

    @Inject
    public TokenAuthUserIdUtil(Config config) {
        this.tokenAuthSubjectPrefix = config.getString("galactic.vote.jwt.token.auth.subject.prefix");
    }

    public boolean isForTokenAuth(String userId) {
        return userId != null && userId.startsWith(tokenAuthSubjectPrefix);
    }

    public UUID getTokenFrom(String userId) {
        String uuidStr = userId.substring(tokenAuthSubjectPrefix.length());
        return UUID.fromString(uuidStr);
    }
}
