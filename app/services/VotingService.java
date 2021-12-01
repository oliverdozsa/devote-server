package services;

import data.entities.JpaVoting;
import data.operations.VotingDbOperations;
import devote.blockchain.Blockchains;
import devote.blockchain.operations.VotingBlockchainOperations;
import ipfs.VotingIpfsOperations;
import requests.CreateVotingRequest;
import responses.VotingResponse;
import play.Logger;
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
            VotingIpfsOperations votingIpfsOperations,
            Blockchains blockchains
    ) {
        this.votingDbOperations = votingDbOperations;
        this.votingBlockchainOperations = votingBlockchainOperations;
        this.votingIpfsOperations = votingIpfsOperations;
        votingResponseFromJpaVoting = new VotingResponseFromJpaVoting(blockchains);
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
                .thenCompose(v -> votingIpfsOperations.saveVotingToIpfs(createdVotingData.id))
                .thenCompose(cid -> votingDbOperations.votingSavedToIpfs(createdVotingData.id, cid))
                .thenApply(v -> createdVotingData.id);
    }

    public CompletionStage<VotingResponse> single(Long id) {
        logger.info("single(): id = {}", id);
        return votingDbOperations.single(id)
                .thenApply(votingResponseFromJpaVoting::convert);
    }

    private static class CreatedVotingData {
        public Long id;
        public List<String> issuerSecrets;
    }
}
