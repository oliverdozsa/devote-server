package data.operations;

import data.repositories.VoterRepository;
import executioncontexts.DatabaseExecutionContext;
import play.Logger;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class VoterDbOperations {
    private static final Logger.ALogger logger = Logger.of(VoterDbOperations.class);

    private final VoterRepository repository;
    private final DatabaseExecutionContext dbExecContext;

    @Inject
    public VoterDbOperations(VoterRepository repository, DatabaseExecutionContext dbExecContext) {
        this.repository = repository;
        this.dbExecContext = dbExecContext;
    }

    public CompletionStage<Void> userAuthenticated(String email, String userId) {
        return runAsync(() -> repository.userAuthenticated(email, userId), dbExecContext);
    }

    public CompletionStage<Boolean> doesParticipateInVoting(String userId, Long votingId) {
        logger.info("doesParticipateInVoting(): userId = {}, votingId = {}", userId, votingId);
        return supplyAsync(() -> repository.doesParticipateInVoting(userId, votingId), dbExecContext);
    }
}
