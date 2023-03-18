package data.operations;

import data.entities.JpaVoting;
import data.repositories.PageOfVotingsRepository;
import executioncontexts.DatabaseExecutionContext;
import play.Logger;
import responses.Page;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class PageOfVotingsDbOperations {
    private final PageOfVotingsRepository repository;
    private final DatabaseExecutionContext dbExecContext;

    private static final Logger.ALogger logger = Logger.of(PageOfVotingsDbOperations.class);

    @Inject
    public PageOfVotingsDbOperations(PageOfVotingsRepository repository, DatabaseExecutionContext dbExecContext) {
        this.repository = repository;
        this.dbExecContext = dbExecContext;
    }

    public CompletionStage<Page<JpaVoting>> pageOfPublic(int offset, int limit) {
        logger.info("pageOfPublic(): offset = {}, limit = {}", offset, limit);
        return supplyAsync(() -> repository.pageOfPublic(offset, limit), dbExecContext);
    }

    public CompletionStage<Page<JpaVoting>> votingsOfVoteCaller(int offset, int limit, String userId) {
        logger.info("votingsOfVoteCaller(): offset = {}, limit = {}, userId = {}", offset, limit, userId);
        return supplyAsync(() -> repository.votingsOfVoteCaller(offset, limit, userId), dbExecContext);
    }

    public CompletionStage<Page<JpaVoting>> votingsOfVoter(int offset, int limit, String userId) {
        logger.info("votingsOfVoter(): offset = {}, limit = {}, userId = {}", offset, limit, userId);
        return supplyAsync(() -> repository.votingsOfVoter(offset, limit, userId), dbExecContext);
    }

    public CompletionStage<Page<JpaVoting>> votingsOfVoterFilteredByNotTriedToCastVote(int offset, int limit, String userId) {
        logger.info("votingsOfVoterFilteredByNotTriedToCastVote(): offset = {}, limit = {}, userId = {}", offset, limit, userId);
        return supplyAsync(() -> repository.votingsOfVoterFilteredByNotTriedToCastVote(offset, limit, userId), dbExecContext);
    }
}
