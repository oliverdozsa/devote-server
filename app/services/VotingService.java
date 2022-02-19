package services;

import data.operations.VotingDbOperations;
import devote.blockchain.api.ChannelGenerator;
import devote.blockchain.operations.VotingBlockchainOperations;
import exceptions.ForbiddenException;
import play.Logger;
import requests.CreateVotingRequest;
import responses.VotingResponse;
import responses.VotingResponseFromJpaVoting;
import security.VerifiedJwt;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.runAsync;


public class VotingService {
    private final VotingDbOperations votingDbOperations;
    private final VotingBlockchainOperations votingBlockchainOperations;
    private final VotingResponseFromJpaVoting votingResponseFromJpaVoting;

    private static final Logger.ALogger logger = Logger.of(VotingService.class);

    @Inject
    public VotingService(
            VotingDbOperations votingDbOperations,
            VotingBlockchainOperations votingBlockchainOperations
    ) {
        this.votingDbOperations = votingDbOperations;
        this.votingBlockchainOperations = votingBlockchainOperations;
        votingResponseFromJpaVoting = new VotingResponseFromJpaVoting();
    }

    public CompletionStage<String> create(CreateVotingRequest request, VerifiedJwt jwt) {
        logger.info("create(): request = {}", request);
        CreatedVotingData createdVotingData = new CreatedVotingData();

        return checkIfUserIsAllowedToCreateVoting(jwt)
                .thenCompose(v -> votingBlockchainOperations.checkFundingAccountOf(request))
                .thenCompose(v -> votingDbOperations.initialize(request))
                .thenAccept(createdVotingData::setId)
                .thenApply(v -> createdVotingData.encodedId);
    }

    public CompletionStage<VotingResponse> single(String id) {
        logger.info("single(): id = {}", id);

        return Base62Conversions.decodeAsStage(id)
                .thenCompose(votingDbOperations::single)
                .thenApply(votingResponseFromJpaVoting::convert);
    }

    private CompletionStage<Void> checkIfUserIsAllowedToCreateVoting(VerifiedJwt jwt) {
        return runAsync(() -> {
            if(!jwt.hasVoteCallerRole()) {
                String message = String.format("User %s is not allowed to create voting.", jwt.getUserId());
                logger.warn("checkIfUserIsAllowedToCreateVoting(): {}", message);
                throw new ForbiddenException(message);
            }
        });
    }

    private static class CreatedVotingData {
        public Long id;
        public List<ChannelGenerator> channelGenerators;
        public String encodedId;

        public void setId(Long id) {
            this.id = id;
            encodedId = Base62Conversions.encode(id);
        }
    }
}
