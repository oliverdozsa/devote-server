package security;

import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.HashSet;
import java.util.Set;

public class VerifiedJwt {
    private final String userId;
    private final Set<String> roles;
    private final String accessToken;

    public VerifiedJwt(DecodedJWT decodedJWT, String accessToken) {
        this.userId = decodedJWT.getSubject();
        this.accessToken = accessToken;

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
