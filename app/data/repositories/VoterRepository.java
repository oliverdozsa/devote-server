package data.repositories;

import data.entities.JpaVoter;

import java.util.List;

public interface VoterRepository {
    void userAuthenticated(String email, String userId);
    boolean doesParticipateInVoting(String userId, Long votingId);
    List<JpaVoter> findThoseWhoNeedsAuthToken(Long votingId, int limit);
}
