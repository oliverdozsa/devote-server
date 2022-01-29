package devote.blockchain.operations;

import devote.blockchain.BlockchainFactory;
import devote.blockchain.Blockchains;
import devote.blockchain.api.DistributionAndBallotAccountOperation;
import devote.blockchain.api.IssuerAccountOperation;
import devote.blockchain.api.KeyPair;
import executioncontexts.BlockchainExecutionContext;
import play.Logger;
import requests.CreateVotingRequest;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static utils.StringUtils.createRandomAlphabeticString;

public class VotingBlockchainOperations {
    private final BlockchainExecutionContext blockchainExecContext;
    private final Blockchains blockchains;

    private static final Logger.ALogger logger = Logger.of(VotingBlockchainOperations.class);

    private static final int MAX_TOKEN_TITLE_BASE_LENGTH = 8;

    @Inject
    public VotingBlockchainOperations(
            BlockchainExecutionContext blockchainExecContext,
            Blockchains blockchains
    ) {
        this.blockchainExecContext = blockchainExecContext;
        this.blockchains = blockchains;
    }

    public CompletionStage<List<KeyPair>> createIssuerAccounts(CreateVotingRequest request) {
        return supplyAsync(() -> {
            logger.info("createIssuerAccounts(): request = {}", request);
            BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(request.getNetwork());
            IssuerAccountOperation issuerAccountOperation = blockchainFactory.createIssuerAccountOperation();

            int numOfAccountsNeeded = issuerAccountOperation.calcNumOfAccountsNeeded(request.getVotesCap());
            List<KeyPair> accountKeyPairs = new ArrayList<>();

            for (int i = 0; i < numOfAccountsNeeded; i++) {
                KeyPair issuerKeyPair = issuerAccountOperation.create(request.getVotesCap(), i + 1);
                accountKeyPairs.add(issuerKeyPair);
            }

            return accountKeyPairs;
        }, blockchainExecContext);
    }

    public CompletionStage<DistributionAndBallotAccountOperation.TransactionResult> createDistributionAndBallotAccounts(
            CreateVotingRequest request,
            List<KeyPair> issuerKeyPairs) {
        return supplyAsync(() -> {
            logger.info("createDistributionAndBallotAccounts(): request = {}, issuers.size = {}", request, issuerKeyPairs.size());

            BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(request.getNetwork());
            DistributionAndBallotAccountOperation distributionAndBallotAccountOperation = blockchainFactory.createDistributionAndBallotAccountOperation();

            List<DistributionAndBallotAccountOperation.IssuerData> issuerData = issuerKeyPairs.stream()
                    .map(kp -> new DistributionAndBallotAccountOperation.IssuerData(generateTokenTitle(request), kp))
                    .collect(Collectors.toList());

            List<String> tokenTitles = issuerData.stream()
                    .map(i -> i.voteTokenTitle)
                    .collect(Collectors.toList());
            logger.info("createDistributionAndBallotAccounts(): About to create distribution and ballot accounts with tokens: {}", tokenTitles);

            return distributionAndBallotAccountOperation.create(issuerData, request.getVotesCap());
        }, blockchainExecContext);
    }

    private static String generateTokenTitle(CreateVotingRequest request) {
        String tokenTitleBase;
        if (request.getTokenIdentifier() == null) {
            tokenTitleBase = request.getTitle();
            tokenTitleBase = tokenTitleBase.replaceAll("[^0-9a-zA-Z]", "");

            if (tokenTitleBase.length() > MAX_TOKEN_TITLE_BASE_LENGTH) {
                tokenTitleBase = tokenTitleBase.substring(0, MAX_TOKEN_TITLE_BASE_LENGTH);
            }
        } else {
            tokenTitleBase = request.getTokenIdentifier();
        }

        tokenTitleBase = tokenTitleBase + "-" + createRandomAlphabeticString(3);
        return tokenTitleBase.toUpperCase(Locale.ROOT);
    }
}
