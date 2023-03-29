package galactic.blockchain.operations;

import galactic.blockchain.BlockchainFactory;
import galactic.blockchain.Blockchains;
import galactic.blockchain.api.FundingAccountOperation;
import exceptions.BusinessLogicViolationException;
import executioncontexts.BlockchainExecutionContext;
import play.Logger;
import requests.CreateVotingRequest;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static utils.StringUtils.redactWithEllipsis;

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

    public CompletionStage<Void> checkFundingAccountOf(CreateVotingRequest createVotingRequest) {
        return runAsync(() -> {
            String loggableAccount = redactWithEllipsis(createVotingRequest.getFundingAccountPublic(), 5);
            logger.info("checkFundingAccountOf(): checking {}", loggableAccount);

            BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(createVotingRequest.getNetwork());
            FundingAccountOperation fundingAccount = blockchainFactory.createFundingAccountOperation();
            if (createVotingRequest.getUseTestnet() != null && createVotingRequest.getUseTestnet()) {
                fundingAccount.useTestNet();
            }

            String fundingAccountPublic = createVotingRequest.getFundingAccountPublic();
            long votesCap = createVotingRequest.getVotesCap();
            if (fundingAccount.doesNotHaveEnoughBalanceForVotesCap(fundingAccountPublic, votesCap)) {
                String message = String.format("%s does not have enough balance for votes cap %d", loggableAccount, votesCap);

                logger.warn("checkFundingAccountOf(): {}", message);
                throw new BusinessLogicViolationException(message);
            } else {
                logger.info("checkFundingAccountOf(): Account {} has enough balance for the voting.", loggableAccount);
            }
        }, blockchainExecContext);
    }
}
