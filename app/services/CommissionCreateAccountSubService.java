package services;

import data.entities.JpaVoting;
import data.entities.JpaVotingChannelAccount;
import data.entities.JpaVotingIssuerAccount;
import data.operations.CommissionDbOperations;
import data.operations.VotingDbOperations;
import devote.blockchain.api.KeyPair;
import devote.blockchain.api.VoterAccount;
import devote.blockchain.operations.CommissionBlockchainOperations;
import play.Logger;
import requests.CommissionAccountCreationRequest;
import responses.CommissionAccountCreationResponse;

import java.util.concurrent.CompletionStage;

class CommissionCreateAccountSubService {
    private final CommissionDbOperations commissionDbOperations;
    private final VotingDbOperations votingDbOperations;
    private final CommissionBlockchainOperations commissionBlockchainOperations;

    public CommissionCreateAccountSubService(
            CommissionDbOperations commissionDbOperations,
            VotingDbOperations votingDbOperations,
            CommissionBlockchainOperations commissionBlockchainOperations
    ) {
        this.commissionDbOperations = commissionDbOperations;
        this.votingDbOperations = votingDbOperations;
        this.commissionBlockchainOperations = commissionBlockchainOperations;
    }

    private static final Logger.ALogger logger = Logger.of(CommissionCreateAccountSubService.class);

    public CompletionStage<CommissionAccountCreationResponse> createAccount(CommissionAccountCreationRequest request) {
        logger.info("createAccount(): request = {}", request);
        ParsedMessage parsedMessage = new ParsedMessage(request.getMessage());

        Long votingId = Base62Conversions.decode(parsedMessage.votingId);
        AccountCreationCollectedData accountCreationData = new AccountCreationCollectedData();
        accountCreationData.voterPublic = parsedMessage.voterPublic;

        return consumeChannel(votingId, accountCreationData)
                .thenCompose(v -> retrieveVoting(votingId, accountCreationData))
                .thenCompose(v -> selectAnIssuer(votingId, accountCreationData))
                .thenApply(v -> assemble(accountCreationData))
                .thenCompose(commissionBlockchainOperations::createTransaction)
                .thenApply(CommissionCreateAccountSubService::toResponse);
    }

    private CompletionStage<Void> consumeChannel(Long votingId, AccountCreationCollectedData collectedData) {
        return commissionDbOperations.consumeOneChannel(votingId)
                .thenAccept(c -> collectedData.channelAccount = c);
    }

    private CompletionStage<Void> retrieveVoting(Long votingId, AccountCreationCollectedData collectedData) {
        return votingDbOperations.single(votingId)
                .thenAccept(v -> collectedData.voting = v);
    }

    private CompletionStage<Void> selectAnIssuer(Long votingId, AccountCreationCollectedData collectedData) {
        return commissionDbOperations.selectAnIssuer(votingId)
                .thenAccept(i -> collectedData.issuer = i);
    }

    private VoterAccount.CreationData assemble(AccountCreationCollectedData accountCreationData) {
        VoterAccount.CreationData voterCreationData = new VoterAccount.CreationData();
        voterCreationData.issuerPublicKey = accountCreationData.issuer.getAccountPublic();
        voterCreationData.assetCode = accountCreationData.issuer.getAssetCode();
        voterCreationData.votesCap = accountCreationData.voting.getVotesCap();
        voterCreationData.channelSecret = accountCreationData.channelAccount.getAccountSecret();
        voterCreationData.voterPublicKey = accountCreationData.voterPublic;
        voterCreationData.distributionKeyPair = new KeyPair(
                accountCreationData.voting.getDistributionAccountSecret(), accountCreationData.voting.getDistributionAccountPublic()
        );

        return voterCreationData;
    }

    private static CommissionAccountCreationResponse toResponse(String transaction) {
        CommissionAccountCreationResponse response = new CommissionAccountCreationResponse();
        response.setTransaction(transaction);

        return response;
    }

    private static class ParsedMessage {
        public final String votingId;
        public final String voterPublic;

        public ParsedMessage(String rawMessage) {
            String[] parts = rawMessage.split("\\|");
            votingId = parts[0];
            voterPublic = parts[1];
        }
    }

    private static class AccountCreationCollectedData {
        public JpaVotingChannelAccount channelAccount;
        public JpaVoting voting;
        public JpaVotingIssuerAccount issuer;
        public String voterPublic;
    }
}
