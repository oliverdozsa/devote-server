package data.operations;

import com.fasterxml.jackson.databind.JsonNode;
import data.entities.JpaVoter;
import data.repositories.VoterRepository;
import executioncontexts.DatabaseExecutionContext;
import play.Logger;
import services.commissionsubs.userinfo.UserInfoCollector;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class VoterDbOperations {
    private static final Logger.ALogger logger = Logger.of(VoterDbOperations.class);

    private final VoterRepository repository;
    private final UserInfoCollector userInfoCollector;
    private final DatabaseExecutionContext dbExecContext;

    @Inject
    public VoterDbOperations(VoterRepository repository, UserInfoCollector userInfoCollector, DatabaseExecutionContext dbExecContext) {
        this.repository = repository;
        this.userInfoCollector = userInfoCollector;
        this.dbExecContext = dbExecContext;
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
        return supplyAsync(() -> repository.doesParticipateInVoting(userId, votingId), dbExecContext);
    }

    private boolean shouldNotCollectUserInfo(String userId) {
        JpaVoter voter = repository.getVoterByUserId(userId);
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
            repository.setUserIdForEmail(email, userId);
            return true;
        }
    }
}
