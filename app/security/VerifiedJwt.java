package security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;

import java.util.Optional;

public class VerifiedJwt {
    private final String userId;
    private final Long votingId;

    public VerifiedJwt(DecodedJWT decodedJWT, Config config) {
        String userIdClaim = config.getString("devote.jwt.useridclaim");
        this.userId = decodedJWT.getClaim(userIdClaim).asString();

        String votingIdClaim = config.getString("devote.jwt.votingidclaim");
        this.votingId = decodedJWT.getClaim(votingIdClaim).asLong();
    }

    public String getUserId() {
        return userId;
    }

    public Optional<Long> getVotingId() {
        return Optional.ofNullable(votingId);
    }
}
