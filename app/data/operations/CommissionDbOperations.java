package data.operations;

import com.fasterxml.jackson.databind.JsonNode;
import data.entities.JpaCommissionSession;
import data.entities.JpaStoredTransaction;
import data.entities.JpaVoter;
import data.entities.JpaVotingChannelAccount;
import data.repositories.CommissionRepository;
import executioncontexts.DatabaseExecutionContext;
import play.Logger;
import services.commissionsubs.userinfo.UserInfoCollector;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static utils.StringUtils.redactWithEllipsis;

public class CommissionDbOperations {
    private final CommissionRepository commissionRepository;
    private final DatabaseExecutionContext dbExecContext;
    private final UserInfoCollector userInfoCollector;

    private static final Logger.ALogger logger = Logger.of(CommissionDbOperations.class);

    @Inject
    public CommissionDbOperations(
            CommissionRepository commissionRepository,
            DatabaseExecutionContext dbExecContext,
            UserInfoCollector userInfoCollector) {
        this.commissionRepository = commissionRepository;
        this.dbExecContext = dbExecContext;
        this.userInfoCollector = userInfoCollector;
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

    public CompletionStage<Void> storeTransaction(Long votingId, String signature, String transaction) {
        String signatureToLog = redactWithEllipsis(signature, 5);
        String transactionToLog = redactWithEllipsis(transaction, 5);
        logger.info("storeTransaction(): votingId = {}, signature = {}, transaction = {}", votingId, signatureToLog, transactionToLog);

        return runAsync(() -> commissionRepository.storeTransactionForRevealedSignature(votingId, signature, transaction),
                dbExecContext);
    }

    public CompletionStage<Boolean> doesTransactionExistForSignature(String signature) {
        logger.info("doesTransactionExistForSignature(): signature = {}", redactWithEllipsis(signature, 5));
        return supplyAsync(() -> commissionRepository.doesTransactionExistForSignature(signature), dbExecContext);
    }

    public CompletionStage<JpaStoredTransaction> getTransaction(String signature) {
        logger.info("getTransaction(): signature = {}", redactWithEllipsis(signature, 5));
        return supplyAsync(() -> commissionRepository.getTransaction(signature), dbExecContext);
    }

    public CompletionStage<JpaCommissionSession> getCommissionSessionWithExistingEnvelopeSignature(Long votingId, String user) {
        logger.info("getCommissionSession(): votingId = {}, user = {}", votingId, user);
        return supplyAsync(() -> commissionRepository.getCommissionSessionWithExistingEnvelopeSignature(votingId, user), dbExecContext);
    }

    public CompletionStage<Boolean> isVotingInitializedProperly(Long votingId) {
        logger.info("isVotingInitializedProperly(): votingId = {}", votingId);
        return supplyAsync(() -> commissionRepository.isVotingInitializedProperly(votingId), dbExecContext);
    }

    public CompletionStage<Void> collectUserInfoIfNeeded(String accessToken, String userId) {
        return runAsync(() -> {
            if(shouldNotCollectUserInfo(userId)) {
                logger.info("collectUserInfo(): Already have info for user: {}", userId);
                return;
            }

            logger.info("collectUserInfoIfNeeded(): collecting info for userId: {}", userId);

            JsonNode userInfoJson = userInfoCollector.collect(accessToken);
            logger.debug("collectUserInfo(): userInfoJson = {}", userInfoJson.toPrettyString());

            if(tryAttachToEmail(userInfoJson)) {
                logger.info("collectUserInfo(): successfully collected info based on email!");
            } else {
                logger.warn("collectUserInfo(): failed to collect user info! userId = {}", userId);
            }
        }, dbExecContext);
    }

    public CompletionStage<Boolean> doesParticipateInVoting(String userId, Long votingId) {
        logger.info("doesParticipateInVoting(): userId = {}, votingId = {}", userId, votingId);
        return supplyAsync(() -> commissionRepository.doesParticipateInVoting(userId, votingId), dbExecContext);
    }

    private boolean shouldNotCollectUserInfo(String userId) {
        JpaVoter voter = commissionRepository.getVoterByUserId(userId);
        return voter != null;
    }

    private boolean tryAttachToEmail(JsonNode userInfoJson) {
        String email = userInfoJson.get("email").isNull() ? "" : userInfoJson.get("email").asText();
        String userId = userInfoJson.get("sub").asText();
        boolean isVerified = userInfoJson.get("email_verified").asBoolean();

        if(email.isEmpty() || !isVerified) {
            String emailInfo = String.format("email: %s, isVerified: %s", email, isVerified);
            logger.warn("tryAttachToEmail(): user email is not valid! {}", emailInfo);
            return false;
        } else {
            commissionRepository.setUserIdForEmail(email, userId);
            return true;
        }
    }
}

