package services;

import data.operations.CommissionDbOperations;
import data.operations.VotingDbOperations;
import data.repositories.VotingRepository;
import devote.blockchain.api.KeyPair;
import play.Logger;
import requests.CommissionAccountCreationRequest;
import responses.CommissionAccountCreationResponse;

import java.util.concurrent.CompletionStage;

class CommissionCreateAccountSubService {
    private final CommissionDbOperations commissionDbOperations;
    private final VotingDbOperations votingDbOperations;

    public CommissionCreateAccountSubService(CommissionDbOperations commissionDbOperations, VotingDbOperations votingDbOperations) {
        this.commissionDbOperations = commissionDbOperations;
        this.votingDbOperations = votingDbOperations;
    }

    private static final Logger.ALogger logger = Logger.of(CommissionCreateAccountSubService.class);

    public CompletionStage<CommissionAccountCreationResponse> createAccount(CommissionAccountCreationRequest request) {
        logger.info("createAccount(): request = {}", request);
        ParsedMessage parsedMessage = new ParsedMessage(request.getMessage());

        Base62Conversions.decodeAsStage(parsedMessage.votingId)
                .thenCompose(commissionDbOperations::consumeOneChannel);
        // TODO:
        //  - Get a voting and collect VoterAccount.CreationData
        //  - Create the transaction string through commission blockchain operations.
        //  - Return the transaction string in response payload (it should be used by the client combined with voter
        //    secret key).

        // TODO
        return null;

    }

    private static class ParsedMessage {
        public final String votingId;
        public final String voterAccount;

        public ParsedMessage(String rawMessage) {
            String[] parts = rawMessage.split("\\|");
            votingId = parts[0];
            voterAccount = parts[1];
        }
    }

    private static class AccountCreationData {
        public KeyPair channelKeyPair;
        public KeyPair distributionKeyPair;
    }
}
