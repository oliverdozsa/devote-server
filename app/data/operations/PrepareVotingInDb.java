package data.operations;

import data.repositories.VotingRepository;
import dto.CreateVotingRequest;
import executioncontexts.DatabaseExecutionContext;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class PrepareVotingInDb {
    private final DatabaseExecutionContext dbExecContext;
    private final VotingRepository votingRepository;

    @Inject
    public PrepareVotingInDb(DatabaseExecutionContext dbExecContext, VotingRepository votingRepository) {
        this.dbExecContext = dbExecContext;
        this.votingRepository = votingRepository;
    }

    public CompletionStage<Long> initialize(CreateVotingRequest createVotingRequest) {
        return supplyAsync(() -> votingRepository.initalize(createVotingRequest), dbExecContext);
    }
}
