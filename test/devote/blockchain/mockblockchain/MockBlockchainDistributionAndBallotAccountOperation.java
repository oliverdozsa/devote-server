package devote.blockchain.mockblockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.DistributionAndBallotAccountOperation;
import devote.blockchain.api.Issuer;
import devote.blockchain.api.Account;

import java.util.List;

public class MockBlockchainDistributionAndBallotAccountOperation implements DistributionAndBallotAccountOperation {
    private static int currentDistributionAccountId = 0;
    private static int currentBallotAccountId = 0;

    @Override
    public void init(BlockchainConfiguration configuration) {
    }

    @Override
    public TransactionResult create(List<Issuer> issuers) {
        currentDistributionAccountId++;
        currentBallotAccountId++;

        String currentDistributionAccountIdAsString = Integer.toString(currentDistributionAccountId);
        String currentBallotAccountIdAsString = Integer.toString(currentBallotAccountId);

        Account distributionAccount = new Account(currentDistributionAccountIdAsString, currentDistributionAccountIdAsString);
        Account ballotAccount = new Account(currentBallotAccountIdAsString, currentBallotAccountIdAsString);

        return new TransactionResult(distributionAccount, ballotAccount);
    }

    public static boolean isDistributionAccountCreated(String account) {
        int accountValue = Integer.parseInt(account);
        return accountValue <= currentDistributionAccountId;
    }

    public static boolean isBallotAccountCreated(String account) {
        int accountValue = Integer.parseInt(account);
        return accountValue <= currentBallotAccountId;
    }
}
