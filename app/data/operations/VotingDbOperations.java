package data.operations;

import data.entities.JpaVoting;
import data.repositories.VotingRepository;
import dto.CreateVotingRequest;
import executioncontexts.DatabaseExecutionContext;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class VotingDbOperations {
    private final DatabaseExecutionContext dbExecContext;
    private final VotingRepository votingRepository;

    @Inject
    public VotingDbOperations(DatabaseExecutionContext dbExecContext, VotingRepository votingRepository) {
        this.dbExecContext = dbExecContext;
        this.votingRepository = votingRepository;
    }

    public CompletionStage<Long> initialize(CreateVotingRequest createVotingRequest) {
        return supplyAsync(() -> votingRepository.initalize(createVotingRequest), dbExecContext);
    }

    public CompletionStage<JpaVoting> single(Long id) {
        return supplyAsync(() -> votingRepository.single(id), dbExecContext);
    }
}
