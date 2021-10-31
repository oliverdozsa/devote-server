package data.operations;

import data.entities.JpaVoting;
import data.repositories.VotingRepository;
import dto.CreateVotingRequest;
import executioncontexts.DatabaseExecutionContext;
import play.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class VotingDbOperations {
    private final DatabaseExecutionContext dbExecContext;
    private final VotingRepository votingRepository;

    private static final Logger.ALogger logger = Logger.of(VotingDbOperations.class);

    @Inject
    public VotingDbOperations(DatabaseExecutionContext dbExecContext, VotingRepository votingRepository) {
        this.dbExecContext = dbExecContext;
        this.votingRepository = votingRepository;
    }

    public CompletionStage<Long> initialize(CreateVotingRequest createVotingRequest) {
        logger.info("initialize(): createVotingRequest = {}", createVotingRequest);
        return supplyAsync(() -> votingRepository.initialize(createVotingRequest), dbExecContext);
    }

    public CompletionStage<JpaVoting> single(Long id) {
        logger.info("single(): id = {}", id);
        return supplyAsync(() -> votingRepository.single(id), dbExecContext);
    }

    public CompletionStage<Void> issuerAccountsCreated(Long id, List<String> accountSecrets) {
        logger.info("issuerAccountsCreated(): id = {}, accountSecrets = {}", id, accountSecrets);
        return runAsync(() -> votingRepository.issuerAccountsCreated(id, accountSecrets), dbExecContext);
    }
}
