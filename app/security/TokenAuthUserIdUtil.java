package security;

import com.typesafe.config.Config;

import javax.inject.Inject;

public class TokenAuthUserIdUtil {
    private final String tokenAuthSubjectPrefix;

    @Inject
    public TokenAuthUserIdUtil(Config config) {
        this.tokenAuthSubjectPrefix = config.getString("devote.jwt.token.auth.subject.prefix");
    }

    public boolean isForTokenAuth(String userId) {
        return userId != null && userId.startsWith(tokenAuthSubjectPrefix);
    }

    public String getTokenFrom(String userId) {
        return userId.substring(tokenAuthSubjectPrefix.length());
    }
}
