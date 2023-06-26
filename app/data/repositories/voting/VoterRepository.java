package data.repositories.voting;

import data.entities.voting.JpaVoter;

import java.util.List;

public interface VoterRepository {
    void userAuthenticated(String email, String userId);
    boolean doesParticipateInVoting(String userId, Long votingId);
    List<JpaVoter> findThoseWhoNeedsAuthToken(Long votingId, int limit);
}
