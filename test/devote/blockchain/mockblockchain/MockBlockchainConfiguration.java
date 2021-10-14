package devote.blockchain.mockblockchain;

import com.typesafe.config.Config;
import devote.blockchain.api.BlockchainConfiguration;

public class MockBlockchainConfiguration implements BlockchainConfiguration {
    private boolean isInitCalled = false;

    @Override
    public String getNetworkName() {
        return "mockblockchain";
    }

    @Override
    public void init(Config config) {
        isInitCalled = true;
    }

    public boolean isInitCalled() {
        return isInitCalled;
    }
}
