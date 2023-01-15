package security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VerifiedJwt {
    private final String userId;
    private final Set<String> roles;
    private final String email;

    public VerifiedJwt(DecodedJWT decodedJWT, Config config) {
        this.userId = decodedJWT.getSubject();

        String rolesClaim = config.getString("devote.jwt.roles.claim");
        List<String> rolesFromJwt = decodedJWT.getClaim(rolesClaim).asList(String.class);

        roles = new HashSet<>(rolesFromJwt);

        String emailClaim = config.getString("devote.jwt.email.claim");
        String emailVerifiedClaim = config.getString("devote.jwt.email.verified.claim");

        String emailFromJwt = decodedJWT.getClaim(emailClaim).asString();
        boolean isVerified = !decodedJWT.getClaim(emailVerifiedClaim).isNull() &&
                decodedJWT.getClaim(emailVerifiedClaim).asBoolean();

        if(isVerified) {
            email = emailFromJwt;
        } else {
            email = "";
        }
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

    public String getEmail() {
        return email;
    }
}
