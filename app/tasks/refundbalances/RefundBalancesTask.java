package tasks.refundbalances;

import data.entities.JpaChannelGeneratorAccount;
import data.entities.JpaVoting;
import data.entities.JpaVotingChannelAccount;
import devote.blockchain.api.Account;
import devote.blockchain.api.RefundBalancesOperation;
import play.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RefundBalancesTask implements Runnable {
    private final RefundBalancesTaskContext context;

    private static final Logger.ALogger logger = Logger.of(RefundBalancesTask.class);

    public RefundBalancesTask(RefundBalancesTaskContext context) {
        this.context = context;
        logger.info("RefundBalancesTask(): created task.");
    }

    @Override
    public void run() {
        try {
            Optional<JpaVoting> optionalVoting = context.votingRepository.findANotRefundedEndedVoting();

            if (optionalVoting.isPresent()) {
                JpaVoting notRefundedVoting = optionalVoting.get();
                logger.info("[REFUND-BALANCES-TASK]: about to refund balances of voting: {}", notRefundedVoting.getId());
                refundVoting(notRefundedVoting);
            } else {
                logger.info("[REFUND-BALANCES-TASK]: not found voting to refund.");
            }
        } catch (Exception e) {
            logger.warn("[REFUND-BALANCES-TASK]: Failed to refund a voting", e);
        }

    }

    private void refundVoting(JpaVoting voting) {
        RefundBalancesOperation blockchainOperation = getOperationFor(voting);

        int maxNumOfAccountsToRefundInOneTx = blockchainOperation.maxNumOfAccountsToRefundInOneTransaction();

        List<JpaVotingChannelAccount> channelAccountsToRefund =
                context.channelAccountRepository.getBatchOfNotRefundedOf(voting.getId(), maxNumOfAccountsToRefundInOneTx);

        if (channelAccountsToRefund.size() > 0) {
            refundChannelAccountsFor(voting, channelAccountsToRefund);
        } else {
            logger.info("[REFUND-BALANCES-TASK]: not found channel accounts to refund for voting: {}; trying to refund " +
                    "channel generators, or distribution.");
            tryRefundChannelGeneratorsOrDistributionOrInternalFunding(voting);
        }
    }

    private void refundChannelAccountsFor(JpaVoting voting, List<JpaVotingChannelAccount> channelAccounts) {
        logger.info("[REFUND-BALANCES-TASK]: about refund channel accounts for voting: {}", voting.getId());
        RefundBalancesOperation blockchainOperation = getOperationFor(voting);

        List<Account> accountsToTerminate = channelAccounts.stream()
                .map(c -> new Account(c.getAccountSecret(), c.getAccountPublic()))
                .collect(Collectors.toList());
        Account destination = new Account(voting.getFundingAccountSecret(), voting.getFundingAccountPublic());

        blockchainOperation.refundBalancesWithTermination(destination, accountsToTerminate);

        List<Long> channelIds = channelAccounts.stream()
                .map(JpaVotingChannelAccount::getId)
                .collect(Collectors.toList());
        context.channelAccountRepository.channelAccountsRefunded(channelIds);
    }

    private void tryRefundChannelGeneratorsOrDistributionOrInternalFunding(JpaVoting voting) {
        RefundBalancesOperation blockchainOperation = getOperationFor(voting);
        int maxNumOfAccountsToRefundInOneTx = blockchainOperation.maxNumOfAccountsToRefundInOneTransaction();

        List<JpaChannelGeneratorAccount> channelGeneratorAccountsToRefund =
                context.channelGeneratorAccountRepository.getBatchOfNotRefundedOf(voting.getId(), maxNumOfAccountsToRefundInOneTx);

        if (channelGeneratorAccountsToRefund.size() > 0) {
            refundChannelGeneratorAccountsFor(voting, channelGeneratorAccountsToRefund);
        } else if(!voting.isDistributionRefunded()){
            logger.info("[REFUND-BALANCES-TASK]: not found channel generator accounts to refund for voting: {}; about to refund" +
                    " distribution account.", voting.getId());
            Account destination = new Account(voting.getFundingAccountSecret(), voting.getFundingAccountPublic());
            Account accountToRefund = new Account(voting.getDistributionAccountSecret(), voting.getDistributionAccountPublic());

            blockchainOperation.refundBalancesWithPayment(destination, Collections.singletonList(accountToRefund));
            context.votingRepository.distributionAccountRefunded(voting.getId());
        } else {
            tryRefundInternalFunding(voting);
        }
    }

    private void refundChannelGeneratorAccountsFor(JpaVoting voting, List<JpaChannelGeneratorAccount> channelGeneratorAccounts) {
        logger.info("[REFUND-BALANCES-TASK]: about refund channel generator accounts for voting: {}", voting.getId());
        RefundBalancesOperation blockchainOperation = getOperationFor(voting);

        List<Account> accountsToTerminate = channelGeneratorAccounts.stream()
                .map(c -> new Account(c.getAccountSecret(), c.getAccountPublic()))
                .collect(Collectors.toList());

        Account destination = new Account(voting.getFundingAccountSecret(), voting.getFundingAccountPublic());

        blockchainOperation.refundBalancesWithTermination(destination, accountsToTerminate);

        List<Long> channelGeneratorIds = channelGeneratorAccounts.stream()
                .map(JpaChannelGeneratorAccount::getId)
                .collect(Collectors.toList());
        context.channelGeneratorAccountRepository.channelGeneratorAccountsRefunded(channelGeneratorIds);
    }

    private void tryRefundInternalFunding(JpaVoting voting) {
        logger.info("[STELLAR]: about to refund internal funding to user's funding for voting: {}", voting.getId());

        RefundBalancesOperation blockchainOperation = getOperationFor(voting);

        Account internalFundingAccount = new Account(voting.getFundingAccountSecret(), voting.getFundingAccountPublic());
        Account fundingAccountOfUser = new Account(voting.getUserGivenFundingAccountSecret(), voting.getUserGivenFundingAccountPublic());

        blockchainOperation.refundBalancesWithTermination(fundingAccountOfUser, Collections.singletonList(internalFundingAccount));

        context.votingRepository.internalFundingAccountRefunded(voting.getId());
    }

    private RefundBalancesOperation getOperationFor(JpaVoting voting) {
        RefundBalancesOperation operation = context.blockchains
                .getFactoryByNetwork(voting.getNetwork())
                .createRefundBalancesOperation();

        if (voting.getOnTestNetwork()) {
            operation.useTestNet();
        }

        return operation;
    }
}
