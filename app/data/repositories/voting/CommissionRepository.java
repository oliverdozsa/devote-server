package data.repositories.voting;

import data.entities.voting.JpaCommissionSession;
import data.entities.voting.JpaStoredTransaction;
import data.entities.voting.JpaVotingChannelAccount;

import java.util.Optional;

public interface CommissionRepository {
    Optional<JpaCommissionSession> getByVotingIdAndUserId(Long votingId, String userId);
    JpaCommissionSession createSession(Long votingId, String userId);
    Boolean hasAlreadySignedAnEnvelope(String userId, Long votingId);
    void storeEnvelopeSignature(String userId, Long votingId, String signature);
    JpaVotingChannelAccount consumeOneChannel(Long votingId);
    void storeTransactionForRevealedSignature(Long votingId, String signature, String transaction);
    boolean doesTransactionExistForSignature(String signature);
    JpaStoredTransaction getTransaction(String signature);
    JpaCommissionSession getCommissionSessionWithExistingEnvelopeSignature(Long votingId, String user);
    boolean isVotingInitializedProperly(Long votingId);
}
