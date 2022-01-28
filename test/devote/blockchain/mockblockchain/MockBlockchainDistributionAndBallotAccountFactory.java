package devote.blockchain.mockblockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.DistributionAndBallotAccountFactory;
import devote.blockchain.api.KeyPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockBlockchainDistributionAndBallotAccountFactory implements DistributionAndBallotAccountFactory {
    private static int currentDistributionAccountId = 0;
    private static int currentBallotAccountId = 0;

    @Override
    public void init(BlockchainConfiguration configuration) {
    }

    @Override
    public TransactionResult create(List<IssuerData> issuerData, Long votesCap) {
        currentDistributionAccountId++;
        currentBallotAccountId++;

        Map<String, String> issuerTokens = new HashMap<>();
        for (IssuerData issuerD : issuerData) {
            issuerTokens.put(issuerD.issuerKeyPair.secretKey, issuerD.voteTokenTitle);
        }

        String currentDistributionAccountIdAsString = Integer.toString(currentDistributionAccountId);
        String currentBallotAccountIdAsString = Integer.toString(currentBallotAccountId);

        KeyPair distributionKeyPair = new KeyPair(currentDistributionAccountIdAsString, currentDistributionAccountIdAsString);
        KeyPair ballotKeyPair = new KeyPair(currentBallotAccountIdAsString, currentBallotAccountIdAsString);

        return new TransactionResult(distributionKeyPair, ballotKeyPair, issuerTokens);
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
