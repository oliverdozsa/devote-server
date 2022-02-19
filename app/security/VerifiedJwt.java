package security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VerifiedJwt {
    private final String userId;
    private final Set<String> roles;
    private final String accessToken;

    public VerifiedJwt(DecodedJWT decodedJWT, String accessToken, Config config) {
        this.userId = decodedJWT.getSubject();
        this.accessToken = accessToken;

        String rolesClaim = config.getString("devote.jwt.roles.claim");
        List<String> rolesFromJwt = decodedJWT.getClaim(rolesClaim).asList(String.class);

        roles = new HashSet<>(rolesFromJwt);
    }

    public String getUserId() {
        return userId;
    }

    public boolean hasVoterRole() {
        return roles.contains("voter");
    }

    public boolean hasVoteCallerRole() {
        return roles.contains("vote-caller");
    }

    public String getAccessToken() {
        return accessToken;
    }
}
