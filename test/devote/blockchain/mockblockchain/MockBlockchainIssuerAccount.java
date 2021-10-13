package devote.blockchain.mockblockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.IssuerAccount;

public class MockBlockchainIssuerAccount implements IssuerAccount {
    @Override
    public void init(BlockchainConfiguration config) {

    }

    @Override
    public String create(long votesCap) {
        // TODO
        return null;
    }
}
