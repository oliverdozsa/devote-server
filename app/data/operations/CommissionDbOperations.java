package data.operations;

import data.entities.JpaCommissionSession;
import data.entities.JpaVoting;
import data.entities.JpaVotingChannelAccount;
import data.entities.JpaVotingIssuerAccount;
import data.repositories.CommissionRepository;
import executioncontexts.DatabaseExecutionContext;
import play.Logger;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.*;

public class CommissionDbOperations {
    private final CommissionRepository commissionRepository;
    private final DatabaseExecutionContext dbExecContext;

    private static final Logger.ALogger logger = Logger.of(CommissionDbOperations.class);

    @Inject
    public CommissionDbOperations(CommissionRepository commissionRepository, DatabaseExecutionContext dbExecContext) {
        this.commissionRepository = commissionRepository;
        this.dbExecContext = dbExecContext;
    }

    public CompletionStage<Boolean> doesSessionExistForUserInVoting(Long votingId, String userId) {
        logger.info("doesSessionExistForUserInVoting(): votingId = {}, userId = {}", votingId, userId);
        return supplyAsync(() -> {
            Optional<JpaCommissionSession> optionalEntity =
                    commissionRepository.getByVotingIdAndUserId(votingId, userId);
            return optionalEntity.isPresent();
        }, dbExecContext);
    }

    public CompletionStage<JpaCommissionSession> createSession(Long votingId, String userId) {
        logger.info("createSession(): votingId = {}, userId = {}", votingId, userId);
        return supplyAsync(() -> commissionRepository.createSession(votingId, userId), dbExecContext);
    }

    public CompletionStage<Boolean> hasAlreadySignedAnEnvelope(String userId, Long votingId) {
        logger.info("hasAlreadySignedAnEnvelope(): userId = {}, votingId = {}", userId, votingId);
        return supplyAsync(() -> commissionRepository.hasAlreadySignedAnEnvelope(userId, votingId), dbExecContext);
    }

    public CompletionStage<String> storeEnvelopeSignature(String userId, Long votingId, String signature) {
        logger.info("storeEnvelopeSignature(): userId = {}, votingId = {}", userId, votingId);
        return supplyAsync(() -> {
            commissionRepository.storeEnvelopeSignature(userId, votingId, signature);
            return signature;
        }, dbExecContext);
    }

    public CompletionStage<JpaVotingChannelAccount> consumeOneChannel(Long votingId) {
        logger.info("consumeOneChannel(): votingId = {}", votingId);
        return supplyAsync(() -> commissionRepository.consumeOneChannel(votingId), dbExecContext);
    }

    public CompletionStage<JpaVotingIssuerAccount> selectAnIssuer(Long votingId) {
        logger.info("selectAnIssuer(): votingId = {}", votingId);
        return supplyAsync(() -> commissionRepository.selectAnIssuer(votingId), dbExecContext);
    }

    public CompletionStage<Void> storeTransaction(Long votingId, String signature, String transaction) {
        String signatureToLog = signature.substring(0, 5);
        String transactionToLog = transaction.substring(0, 5);
        logger.info("storeTransaction(): votingId = {}, signature = {}, transaction = {}", votingId, signatureToLog, transactionToLog);

        return runAsync(() -> commissionRepository.storeTransactionForRevealedSignature(votingId, signature, transaction),
                dbExecContext);
    }

    public CompletionStage<Boolean> doesTransactionExistForSignature(String signature) {
        String signatureToLog = signature.substring(0, 5);
        logger.info("doesTransactionExistForSignature(): signature = {}", signatureToLog);
        return supplyAsync(() -> commissionRepository.doesTransactionExistForSignature(signature), dbExecContext);
    }
}

