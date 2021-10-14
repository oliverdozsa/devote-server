package devote.blockchain.mockblockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.IssuerAccount;

public class MockBlockchainIssuerAccount implements IssuerAccount {
    private MockBlockchainConfiguration config;
    private boolean isInitCalled = false;

    @Override
    public void init(BlockchainConfiguration config) {
        this.config = (MockBlockchainConfiguration) config;
        isInitCalled = true;
    }

    @Override
    public String create(long votesCap) {
        // TODO
        return null;
    }

    public MockBlockchainConfiguration getConfig() {
        return config;
    }

    public boolean isInitCalled() {
        return isInitCalled;
    }
}
