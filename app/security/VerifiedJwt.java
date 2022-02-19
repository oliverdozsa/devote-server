package security;

import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.HashSet;
import java.util.Set;

public class VerifiedJwt {
    private final String userId;
    private final Set<String> roles;

    public VerifiedJwt(DecodedJWT decodedJWT) {
        this.userId = decodedJWT.getSubject();

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
