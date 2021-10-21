package data.operations;

import devote.blockchain.BlockchainFactory;
import devote.blockchain.Blockchains;
import devote.blockchain.api.IssuerAccount;
import dto.CreateVotingRequest;
import executioncontexts.BlockchainExecutionContext;
import play.Logger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class VotingBlockchainOperations {
    private final BlockchainExecutionContext blockchainExecContext;
    private final Blockchains blockchains;

    private static final Logger.ALogger logger = Logger.of(VotingBlockchainOperations.class);

    @Inject
    public VotingBlockchainOperations(
            BlockchainExecutionContext blockchainExecContext,
            Blockchains blockchains
    ) {
        this.blockchainExecContext = blockchainExecContext;
        this.blockchains = blockchains;
    }

    public CompletionStage<List<String>> createIssuerAccounts(CreateVotingRequest request) {
        logger.info("createIssuerAccounts(): request = {}", request);
        return supplyAsync(() -> {
            BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(request.getNetwork());
            IssuerAccount issuerAccount = blockchainFactory.createIssuerAccount();

            int numOfAccountsNeeded = issuerAccount.calcNumOfAccountsNeeded(request.getVotesCap());
            List<String> accountSecrets = new ArrayList<>();

            for(int i = 0; i < numOfAccountsNeeded; i++) {
                String accountSecret = issuerAccount.create(request.getVotesCap());
                accountSecrets.add(accountSecret);
            }

            return accountSecrets;
        }, blockchainExecContext);
    }
}
