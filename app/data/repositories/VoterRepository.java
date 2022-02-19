package data.repositories;

import data.entities.JpaVoter;

public interface VoterRepository {
    void setUserIdForEmail(String email, String userId);
    JpaVoter getVoterByUserId(String userId);
    boolean doesParticipateInVoting(String userId, Long votingId);
}
