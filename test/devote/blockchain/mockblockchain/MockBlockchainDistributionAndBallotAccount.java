package devote.blockchain.mockblockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.DistributionAndBallotAccount;

public class MockBlockchainDistributionAndBallotAccount implements DistributionAndBallotAccount {
    @Override
    public void init(BlockchainConfiguration configuration) {
    }

    @Override
    public TransactionResult create(IssuerData[] issuerData, Long votesCap) {
        // TODO
        return null;
    }
}
