package data.operations.voting;

import data.entities.voting.JpaVoting;
import data.repositories.voting.VotingRepository;
import executioncontexts.DatabaseExecutionContext;
import play.Logger;
import requests.voting.CreateVotingRequest;

import javax.inject.Inject;
import java.util.Locale;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static utils.StringUtils.createRandomAlphabeticString;

public class VotingDbOperations {
    private final DatabaseExecutionContext dbExecContext;
    private final VotingRepository votingRepository;

    private static final Logger.ALogger logger = Logger.of(VotingDbOperations.class);

    private static final int MAX_TOKEN_TITLE_BASE_LENGTH = 8;

    @Inject
    public VotingDbOperations(DatabaseExecutionContext dbExecContext, VotingRepository votingRepository) {
        this.dbExecContext = dbExecContext;
        this.votingRepository = votingRepository;
    }

    public CompletionStage<Long> initialize(CreateVotingRequest createVotingRequest, String userId) {
        return supplyAsync(() -> {
            logger.info("initialize(): userId = {}, createVotingRequest = {}", userId, createVotingRequest);
            return votingRepository.initialize(createVotingRequest, generateAssetCode(createVotingRequest), userId);
        }, dbExecContext);
    }

    public CompletionStage<JpaVoting> single(Long id) {
        return supplyAsync(() -> {
            logger.info("single(): id = {}", id);
            return votingRepository.single(id);
        }, dbExecContext);
    }

    private static String generateAssetCode(CreateVotingRequest request) {
        String titleBase;
        if (request.getTokenIdentifier() == null) {
            titleBase = request.getTitle();
            titleBase = titleBase.replaceAll("[^0-9a-zA-Z]", "");

            if (titleBase.length() > MAX_TOKEN_TITLE_BASE_LENGTH) {
                titleBase = titleBase.substring(0, MAX_TOKEN_TITLE_BASE_LENGTH);
            }
        } else {
            titleBase = request.getTokenIdentifier();
        }

        titleBase = titleBase + createRandomAlphabeticString(4);
        return titleBase.toUpperCase(Locale.ROOT);
    }
}
