package data.repositories;

import data.entities.JpaCommissionInitSession;

import java.util.Optional;

public interface CommissionRepository {
    Optional<JpaCommissionInitSession> getByVotingIdAndUserId(Long votingId, String userId);
    JpaCommissionInitSession createSession(Long votingId, String userId);
}
