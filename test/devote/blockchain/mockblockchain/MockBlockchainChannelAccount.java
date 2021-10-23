package devote.blockchain.mockblockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.ChannelAccount;

public class MockBlockchainChannelAccount implements ChannelAccount {

    @Override
    public void init(BlockchainConfiguration configuration) {
        // TODO
    }

    @Override
    public int numOfAccountsToCreateInOneBatch() {
        // TODO
        return 0;
    }

    @Override
    public String create(long votesCap, String issuerSecret) {
        // TODO
        return null;
    }
}
