package services;

import data.operations.VotingDbOperations;
import devote.blockchain.api.Issuer;
import devote.blockchain.operations.VotingBlockchainOperations;
import ipfs.VotingIpfsOperations;
import play.Logger;
import requests.CreateVotingRequest;
import responses.VotingResponse;
import responses.VotingResponseFromJpaVoting;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;


public class VotingService {
    private final VotingDbOperations votingDbOperations;
    private final VotingBlockchainOperations votingBlockchainOperations;
    private final VotingIpfsOperations votingIpfsOperations;
    private final VotingResponseFromJpaVoting votingResponseFromJpaVoting;

    private static final Logger.ALogger logger = Logger.of(VotingService.class);

    @Inject
    public VotingService(
            VotingDbOperations votingDbOperations,
            VotingBlockchainOperations votingBlockchainOperations,
            VotingIpfsOperations votingIpfsOperations
    ) {
        this.votingDbOperations = votingDbOperations;
        this.votingBlockchainOperations = votingBlockchainOperations;
        this.votingIpfsOperations = votingIpfsOperations;
        votingResponseFromJpaVoting = new VotingResponseFromJpaVoting();
    }

    public CompletionStage<String> create(CreateVotingRequest request) {
        logger.info("create(): request = {}", request);
        CreatedVotingData createdVotingData = new CreatedVotingData();

        return votingDbOperations
                .initialize(request)
                .thenAccept(createdVotingData::setId)
                .thenCompose(v -> votingBlockchainOperations.createIssuerAccounts(request))
                .thenAccept(issuers -> createdVotingData.issuers = issuers)
                .thenCompose(v -> votingDbOperations.issuerAccountsCreated(createdVotingData.id, createdVotingData.issuers))
                .thenCompose(v -> votingBlockchainOperations.createDistributionAndBallotAccounts(request.getNetwork(), createdVotingData.issuers))
                .thenCompose(tr -> votingDbOperations.distributionAndBallotAccountsCreated(createdVotingData.id, tr))
                .thenCompose(v -> votingIpfsOperations.saveVotingToIpfs(createdVotingData.id))
                .thenCompose(cid -> votingDbOperations.votingSavedToIpfs(createdVotingData.id, cid))
                .thenApply(v -> createdVotingData.encodedId);
    }

    public CompletionStage<VotingResponse> single(String id) {
        logger.info("single(): id = {}", id);

        return Base62Conversions.decodeAsStage(id)
                .thenCompose(votingDbOperations::single)
                .thenApply(votingResponseFromJpaVoting::convert);
    }

    private static class CreatedVotingData {
        public Long id;
        public List<Issuer> issuers;
        public String encodedId;

        public void setId(Long id) {
            this.id = id;
            encodedId = Base62Conversions.encode(id);
        }
    }
}
