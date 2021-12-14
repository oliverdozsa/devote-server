package data.repositories;

import data.entities.JpaCommissionInitSession;

import java.util.Optional;

public interface CommissionRepository {
    Optional<JpaCommissionInitSession> getByVotingAndUserId(Long votingId, String userId);
    JpaCommissionInitSession create(Long votingId, String userId);
}
