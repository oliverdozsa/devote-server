package data.repositories;

import data.entities.JpaCommissionSession;

import java.util.Optional;

public interface CommissionRepository {
    Optional<JpaCommissionSession> getByVotingIdAndUserId(Long votingId, String userId);
    JpaCommissionSession createSession(Long votingId, String userId);
    Boolean hasAlreadySignedAnEnvelope(String userId, Long votingId);
    void storeEnvelopeSignature(String userId, Long votingId, String signature);
}
