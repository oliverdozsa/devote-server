package data.repositories.voting;

import data.entities.voting.JpaAuthToken;

public interface TokenAuthRepository {
    JpaAuthToken findBy(String token);
    JpaAuthToken createFor(Long votingId, Long userId);
    void cleanupOlderThanFromVotingEndDate(int days, int limit);
}
