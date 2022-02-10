package devote.blockchain.operations;

import devote.blockchain.BlockchainFactory;
import devote.blockchain.Blockchains;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.DistributionAndBallotAccountOperation;
import devote.blockchain.api.FundingAccountOperation;
import devote.blockchain.api.Issuer;
import devote.blockchain.api.IssuerAccountOperation;
import devote.blockchain.api.Account;
import executioncontexts.BlockchainExecutionContext;
import play.Logger;
import requests.CreateVotingRequest;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static utils.StringUtils.createRandomAlphabeticString;
import static utils.StringUtils.redactWithEllipsis;

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

    public CompletionStage<List<Issuer>> createIssuerAccounts(CreateVotingRequest request) {
        return supplyAsync(() -> {
            logger.info("createIssuerAccounts(): request = {}", request);
            BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(request.getNetwork());
            IssuerAccountOperation issuerAccountOperation = blockchainFactory.createIssuerAccountOperation();

            long numOfAccountsNeeded = issuerAccountOperation.calcNumOfAccountsNeeded(request.getVotesCap());

            long votesCapPerIssuer = request.getVotesCap() / numOfAccountsNeeded;
            long votesCapRemainder = request.getVotesCap() % numOfAccountsNeeded;

            List<Issuer> issuers = new ArrayList<>();
            List<String> assetCodes = generateUniqueAssetCodes(request, numOfAccountsNeeded);
            long votesCapForIssuer = votesCapPerIssuer;

            Account funding = new Account(request.getFundingAccountSecret(), request.getFundingAccountPublic());
            for (int i = 0; i < numOfAccountsNeeded - 1; i++) {
                Account issuerAccount = issuerAccountOperation.create(votesCapForIssuer, funding);
                issuers.add(new Issuer(issuerAccount, votesCapForIssuer, assetCodes.get(i)));
            }

            // For last one the remainder is also needed
            votesCapForIssuer = votesCapPerIssuer + votesCapRemainder;
            Account issuerAccount = issuerAccountOperation.create(votesCapForIssuer, funding);
            issuers.add(new Issuer(issuerAccount, votesCapForIssuer, assetCodes.get(assetCodes.size() - 1)));

            return issuers;
        }, blockchainExecContext);
    }

    public CompletionStage<DistributionAndBallotAccountOperation.TransactionResult> createDistributionAndBallotAccounts(
            String network,
            List<Issuer> issuers) {
        return supplyAsync(() -> {
            logger.info("createDistributionAndBallotAccounts(): network = {}, issuers.size = {}", network, issuers.size());

            BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(network);
            DistributionAndBallotAccountOperation distributionAndBallotAccountOperation = blockchainFactory.createDistributionAndBallotAccountOperation();

            List<String> tokenTitles = issuers.stream()
                    .map(issuer -> issuer.assetCode)
                    .collect(Collectors.toList());
            logger.info("createDistributionAndBallotAccounts(): About to create distribution and ballot accounts with tokens: {}", tokenTitles);

            return distributionAndBallotAccountOperation.create(issuers);
        }, blockchainExecContext);
    }

    public CompletionStage<Void> checkFundingAccountOf(CreateVotingRequest createVotingRequest) {
        return runAsync(() -> {
            String loggableAccount = redactWithEllipsis(createVotingRequest.getFundingAccountPublic(), 5);
            logger.info("checkFundingAccountOf(): checking {}", loggableAccount);

            BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(createVotingRequest.getNetwork());
            FundingAccountOperation fundingAccountOperation = blockchainFactory.createFundingAccountOperation();

            String fundingAccountPublic = createVotingRequest.getFundingAccountPublic();
            long votesCap = createVotingRequest.getVotesCap();
            if(fundingAccountOperation.doesAccountNotHaveEnoughBalanceForVotesCap(fundingAccountPublic, votesCap)) {
                String message = String.format("%s does not have enough balance for votes cap %d", loggableAccount, votesCap);

                logger.warn("checkFundingAccountOf(): {}", message);
                throw new BlockchainException(message);
            } else {
                logger.info("checkFundingAccountOf(): Account {} has enough balance for the voting.", loggableAccount);
            }
        }, blockchainExecContext);
    }

    private static List<String> generateUniqueAssetCodes(CreateVotingRequest request, long numsToCreate) {
        Set<String> uniqueAssetCodes = new HashSet<>();

        while (uniqueAssetCodes.size() != numsToCreate) {
            uniqueAssetCodes.add(generateAssetCode(request));
        }

        return new ArrayList<>(uniqueAssetCodes);
    }

    private static String generateAssetCode(CreateVotingRequest request) {
        String titleBase;
        if (request.getTokenIdentifier() == null) {
            titleBase = request.getTitle();
            titleBase = titleBase.replaceAll("[^0-9a-zA-Z]", "");

            if (titleBase.length() > MAX_TOKEN_TITLE_BASE_LENGTH) {
                titleBase = titleBase.substring(0, MAX_TOKEN_TITLE_BASE_LENGTH);
            }
        } else {
            titleBase = request.getTokenIdentifier();
        }

        titleBase = titleBase + "-" + createRandomAlphabeticString(3);
        return titleBase.toUpperCase(Locale.ROOT);
    }

}
