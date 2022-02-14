package devote.blockchain.operations;

import devote.blockchain.BlockchainFactory;
import devote.blockchain.Blockchains;
import devote.blockchain.api.Account;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.ChannelGenerator;
import devote.blockchain.api.ChannelGeneratorAccountOperation;
import devote.blockchain.api.DistributionAndBallotAccountOperation;
import devote.blockchain.api.FundingAccountOperation;
import executioncontexts.BlockchainExecutionContext;
import play.Logger;
import requests.CreateVotingRequest;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionStage;

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

    public CompletionStage<List<ChannelGenerator>> createChannelGeneratorAccounts(CreateVotingRequest request) {
        return supplyAsync(() -> {
            logger.info("createChannelGeneratorAccounts(): request = {}", request);
            BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(request.getNetwork());
            ChannelGeneratorAccountOperation channelGeneratorAccountOperation = blockchainFactory.createChannelGeneratorAccountOperation();

            Account funding = new Account(request.getFundingAccountSecret(), request.getFundingAccountPublic());
            return channelGeneratorAccountOperation.create(request.getVotesCap(), funding);
        }, blockchainExecContext);
    }

    public CompletionStage<BallotAndDistributionResult> createDistributionAndBallotAccounts(CreateVotingRequest request) {
        return supplyAsync(() -> {
            String network = request.getNetwork();
            logger.info("createDistributionAndBallotAccounts(): network = {}", network);

            BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(network);
            DistributionAndBallotAccountOperation distributionAndBallotAccountOperation = blockchainFactory.createDistributionAndBallotAccountOperation();

            Account funding = new Account(request.getFundingAccountSecret(), request.getFundingAccountPublic());
            String assetCode = generateAssetCode(request);

            DistributionAndBallotAccountOperation.TransactionResult txResult = distributionAndBallotAccountOperation.create(funding, assetCode, request.getVotesCap());
            return new BallotAndDistributionResult(txResult, assetCode);
        }, blockchainExecContext);
    }

    public CompletionStage<Void> checkFundingAccountOf(CreateVotingRequest createVotingRequest) {
        return runAsync(() -> {
            String loggableAccount = redactWithEllipsis(createVotingRequest.getFundingAccountPublic(), 5);
            logger.info("checkFundingAccountOf(): checking {}", loggableAccount);

            BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(createVotingRequest.getNetwork());
            FundingAccountOperation fundingAccount = blockchainFactory.createFundingAccountOperation();

            String fundingAccountPublic = createVotingRequest.getFundingAccountPublic();
            long votesCap = createVotingRequest.getVotesCap();
            if (fundingAccount.doesNotHaveEnoughBalanceForVotesCap(fundingAccountPublic, votesCap)) {
                String message = String.format("%s does not have enough balance for votes cap %d", loggableAccount, votesCap);

                logger.warn("checkFundingAccountOf(): {}", message);
                throw new BlockchainException(message);
            } else {
                logger.info("checkFundingAccountOf(): Account {} has enough balance for the voting.", loggableAccount);
            }
        }, blockchainExecContext);
    }

    public static class BallotAndDistributionResult {
        public final DistributionAndBallotAccountOperation.TransactionResult transactionResult;
        public final String assetCode;

        public BallotAndDistributionResult(DistributionAndBallotAccountOperation.TransactionResult transactionResult, String assetCode) {
            this.transactionResult = transactionResult;
            this.assetCode = assetCode;
        }
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

        titleBase = titleBase + createRandomAlphabeticString(4);
        return titleBase.toUpperCase(Locale.ROOT);
    }

}
