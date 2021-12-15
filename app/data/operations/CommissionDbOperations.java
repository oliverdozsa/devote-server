package data.operations;

import data.entities.JpaCommissionInitSession;
import data.repositories.CommissionRepository;
import executioncontexts.DatabaseExecutionContext;
import play.Logger;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

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
            Optional<JpaCommissionInitSession> optionalEntity =
                    commissionRepository.getByVotingIdAndUserId(votingId, userId);
            return optionalEntity.isPresent();
        }, dbExecContext);
    }

    public CompletionStage<JpaCommissionInitSession> createSession(Long votingId, String userId) {
        logger.info("createSession(): votingId = {}, userId = {}", votingId, userId);
        return supplyAsync(() -> commissionRepository.createSession(votingId, userId), dbExecContext);
    }
}

