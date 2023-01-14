package data.operations;

import data.entities.JpaAuthToken;
import data.repositories.TokenAuthRepository;
import executioncontexts.DatabaseExecutionContext;
import play.Logger;
import utils.StringUtils;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class TokenAuthDbOperations {
    private static final Logger.ALogger logger = Logger.of(TokenAuthDbOperations.class);

    private final TokenAuthRepository repository;
    private final DatabaseExecutionContext dbExecContext;

    @Inject
    public TokenAuthDbOperations(TokenAuthRepository repository, DatabaseExecutionContext dbExecContext) {
        this.repository = repository;
        this.dbExecContext = dbExecContext;
    }

    public CompletionStage<JpaAuthToken> findBy(String token) {
        logger.info("findBy(): token = {}", StringUtils.redactWithEllipsis(token, 5));
        return supplyAsync(() -> repository.findBy(token), dbExecContext);
    }

    public CompletionStage<JpaAuthToken> createFor(Long votingId, Long userId) {
        logger.info("createFor(): votingId = {}, userId = {}", votingId, userId);
        return supplyAsync(() -> repository.createFor(votingId, userId), dbExecContext);
    }
}
