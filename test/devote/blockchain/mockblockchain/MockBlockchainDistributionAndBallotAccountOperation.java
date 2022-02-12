package devote.blockchain.mockblockchain;

import devote.blockchain.api.Account;
import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.DistributionAndBallotAccountOperation;

public class MockBlockchainDistributionAndBallotAccountOperation implements DistributionAndBallotAccountOperation {
    private static int currentDistributionAccountId = 0;
    private static int currentBallotAccountId = 0;
    private static int currentIssuerAccountId = 0;

    @Override
    public void init(BlockchainConfiguration configuration) {
    }

    @Override
    public TransactionResult create(Account funding, String assetCode, long votesCap) {
        currentDistributionAccountId++;
        currentBallotAccountId++;
        currentIssuerAccountId++;

        String currentDistributionAccountIdAsString = Integer.toString(currentDistributionAccountId);
        String currentBallotAccountIdAsString = Integer.toString(currentBallotAccountId);
        String currentIssuerAccountIdAsString = Integer.toString(currentIssuerAccountId);

        Account distributionAccount = new Account(currentDistributionAccountIdAsString, currentDistributionAccountIdAsString);
        Account ballotAccount = new Account(currentBallotAccountIdAsString, currentBallotAccountIdAsString);
        Account issuerAccount = new Account(currentIssuerAccountIdAsString, currentIssuerAccountIdAsString);

        return new TransactionResult(distributionAccount, ballotAccount, issuerAccount);
    }

    public static boolean isDistributionAccountCreated(String account) {
        int accountValue = Integer.parseInt(account);
        return accountValue <= currentDistributionAccountId;
    }

    public static boolean isBallotAccountCreated(String account) {
        int accountValue = Integer.parseInt(account);
        return accountValue <= currentBallotAccountId;
    }

    public static boolean isIssuerAccountCreated(String account) {
        int accountValue = Integer.parseInt(account);
        return accountValue <= currentIssuerAccountId;
    }
}
