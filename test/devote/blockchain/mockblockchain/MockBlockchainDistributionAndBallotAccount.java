package devote.blockchain.mockblockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.DistributionAndBallotAccount;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<String, String> issuerTokens = new HashMap<>();
        for(IssuerData issuerD: issuerData) {
            issuerTokens.put(issuerD.issuerSecret, issuerD.voteTokenTitle);
        }

        return new TransactionResult(Integer.toString(currentDistributionAccountId), Integer.toString(currentBallotAccountId), issuerTokens);
    }

    @Override
    public String toPublicBallotAccountId(String ballotSecret) {
        return "MOCK_BALLOT_PUBLIC_" + ballotSecret;
    }

    @Override
    public String toPublicDistributionAccountId(String distributionSecret) {
        return "MOCK_BALLOT_PUBLIC_" + distributionSecret;
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
