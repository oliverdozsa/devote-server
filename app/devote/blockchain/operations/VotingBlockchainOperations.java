package devote.blockchain.operations;

import devote.blockchain.BlockchainFactory;
import devote.blockchain.Blockchains;
import devote.blockchain.api.DistributionAndBallotAccount;
import devote.blockchain.api.IssuerAccount;
import requests.CreateVotingRequest;
import executioncontexts.BlockchainExecutionContext;
import play.Logger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;

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

    public CompletionStage<List<String>> createIssuerAccounts(CreateVotingRequest request) {
        return supplyAsync(() -> {
            logger.info("createIssuerAccounts(): request = {}", request);
            BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(request.getNetwork());
            IssuerAccount issuerAccount = blockchainFactory.createIssuerAccount();

            int numOfAccountsNeeded = issuerAccount.calcNumOfAccountsNeeded(request.getVotesCap());
            List<String> accountSecrets = new ArrayList<>();

            for (int i = 0; i < numOfAccountsNeeded; i++) {
                String accountSecret = issuerAccount.create(request.getVotesCap());
                accountSecrets.add(accountSecret);
            }

            return accountSecrets;
        }, blockchainExecContext);
    }

    public CompletionStage<DistributionAndBallotAccount.TransactionResult> createDistributionAndBallotAccounts(
            CreateVotingRequest request,
            List<String> issuers) {
        return supplyAsync(() -> {
            logger.info("createDistributionAndBallotAccounts(): request = {}, issuers.size = {}", request, issuers.size());

            BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(request.getNetwork());
            DistributionAndBallotAccount distributionAndBallotAccount = blockchainFactory.createDistributionAndChannelAccount();

            List<DistributionAndBallotAccount.IssuerData> issuerData = issuers.stream()
                    .map(is -> new DistributionAndBallotAccount.IssuerData(generateTokenTitle(request), is))
                    .collect(Collectors.toList());

            List<String> tokenTitles = issuerData.stream()
                    .map(i -> i.voteTokenTitle)
                    .collect(Collectors.toList());
            logger.info("createDistributionAndBallotAccounts(): About to create distribution and ballot accounts with tokens: {}", tokenTitles);

            return distributionAndBallotAccount.create(issuerData, request.getVotesCap());
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

    private static String createRandomAlphabeticString(int ofLength) {
        int letterACode = 97;
        int letterZCode = 122;

        Random random = new Random();

        return random.ints(letterACode, letterZCode + 1)
                .limit(ofLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
