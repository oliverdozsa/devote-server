package data.repositories;

import data.entities.JpaCommissionSession;
import data.entities.JpaVotingChannelAccount;
import data.entities.JpaVotingIssuerAccount;

import java.util.Optional;

public interface CommissionRepository {
    Optional<JpaCommissionSession> getByVotingIdAndUserId(Long votingId, String userId);
    JpaCommissionSession createSession(Long votingId, String userId);
    Boolean hasAlreadySignedAnEnvelope(String userId, Long votingId);
    void storeEnvelopeSignature(String userId, Long votingId, String signature);
    JpaVotingChannelAccount consumeOneChannel(Long votingId);
    JpaVotingIssuerAccount selectAnIssuer(Long votingId);
    void storeTransactionForRevealedSignature(Long votingId, String signature, String transaction);
    boolean doesTransactionExistForSignature(String signature);
}
