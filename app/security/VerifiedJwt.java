package security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class VerifiedJwt {
    private final String userId;
    private final Set<String> roles;

    public VerifiedJwt(DecodedJWT decodedJWT, Config config) {
        String userIdClaim = config.getString("devote.jwt.useridclaim");
        this.userId = decodedJWT.getClaim(userIdClaim).asString();

        // TODO
        roles = new HashSet<>();

    }

    public String getUserId() {
        return userId;
    }

    public boolean hasVoterRole() {
        // TODO
        return true;
    }

    public boolean hasVoteCallerRole() {
        // TODO
        return true;
    }

    public String accessToken() {
        // TODO
        return "";
    }
}
