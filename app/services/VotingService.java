package services;

import data.entities.JpaVoting;
import data.operations.VotingDbOperations;
import devote.blockchain.operations.VotingBlockchainOperations;
import requests.CreateVotingRequest;
import responses.SingleVotingResponse;
import play.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class VotingService {
    private final VotingDbOperations votingDbOperations;
    private final VotingBlockchainOperations votingBlockchainOperations;

    private static final Logger.ALogger logger = Logger.of(VotingService.class);

    @Inject
    public VotingService(
            VotingDbOperations votingDbOperations,
            VotingBlockchainOperations votingBlockchainOperations
    ) {
        this.votingDbOperations = votingDbOperations;
        this.votingBlockchainOperations = votingBlockchainOperations;
    }

    public CompletionStage<Long> create(CreateVotingRequest request) {
        logger.info("create(): request = {}", request);
        CreatedVotingData createdVotingData = new CreatedVotingData();

        return votingDbOperations
                .initialize(request)
                .thenAccept(id -> createdVotingData.id = id)
                .thenCompose(v -> votingBlockchainOperations.createIssuerAccounts(request))
                .thenAccept(issuers -> createdVotingData.issuerSecrets = issuers)
                .thenCompose(v -> votingDbOperations.issuerAccountsCreated(createdVotingData.id, createdVotingData.issuerSecrets))
                .thenCompose(v -> votingBlockchainOperations.createDistributionAndBallotAccounts(request, createdVotingData.issuerSecrets))
                .thenCompose(tr -> votingDbOperations.distributionAndBallotAccountsCreated(createdVotingData.id, tr))
                .thenApply(v -> createdVotingData.id);
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

    private static class CreatedVotingData {
        public Long id;
        public List<String> issuerSecrets;
    }
}
