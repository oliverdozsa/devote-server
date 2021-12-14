package security;

import java.util.Optional;

public interface VerifiedJwt {
    String getUserId();
    Optional<Long> getVotingId();
}
