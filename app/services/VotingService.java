package services;

import com.typesafe.config.Config;
import data.entities.JpaVoting;
import devote.blockchain.operations.VotingBlockchainOperations;
import data.operations.VotingDbOperations;
import dto.CreateVotingRequest;
import dto.SingleVotingResponse;
import play.Logger;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class VotingService {
    private final Config config;
    private final VotingDbOperations votingDbOperations;
    private final VotingBlockchainOperations votingBlockchainOperations;

    private static final Logger.ALogger logger = Logger.of(VotingService.class);

    @Inject
    public VotingService(
            Config config,
            VotingDbOperations votingDbOperations,
            VotingBlockchainOperations votingBlockchainOperations
    ) {
        this.config = config;
        this.votingDbOperations = votingDbOperations;
        this.votingBlockchainOperations = votingBlockchainOperations;
    }

    public CompletionStage<Long> create(CreateVotingRequest request) {
        logger.info("create(): request = {}", request);
        CreatedVotingId votingId = new CreatedVotingId();

        return votingDbOperations
                .initialize(request)
                .thenAccept(id -> votingId.id = id)
                .thenCompose(v -> votingBlockchainOperations.createIssuerAccounts(request))
                .thenAccept(accountSecrets -> votingDbOperations.issuerAccountsCreated(votingId.id, accountSecrets))
                .thenApply(o -> votingId.id);
    }

    public CompletionStage<SingleVotingResponse> single(Long id) {
        logger.info("single(): id = {}", id);
        return votingDbOperations.single(id)
                .thenApply(VotingService::toSingleVotingResponse);
    }

    private static SingleVotingResponse toSingleVotingResponse(JpaVoting entity) {
        SingleVotingResponse votingResponse = new SingleVotingResponse();
        votingResponse.setId(entity.getId());
        votingResponse.setNetwork(entity.getNetwork());
        return votingResponse;
    }

    private static class CreatedVotingId {
        public Long id;
    }
}
