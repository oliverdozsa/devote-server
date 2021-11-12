package devote.blockchain.mockblockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.DistributionAndBallotAccount;

import java.util.List;

public class MockBlockchainDistributionAndBallotAccount implements DistributionAndBallotAccount {
    private static int currentDistributionAccountId = 0;
    private static int currentBallotAccountId = 0;

    @Override
    public void init(BlockchainConfiguration configuration) {
    }

    @Override
    public TransactionResult create(List<IssuerData> issuerData, Long votesCap) {
        currentDistributionAccountId++;
        currentBallotAccountId++;
        return new TransactionResult(Integer.toString(currentDistributionAccountId), Integer.toString(currentBallotAccountId));
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
