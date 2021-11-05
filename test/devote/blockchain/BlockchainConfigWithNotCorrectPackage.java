package devote.blockchain;

import com.typesafe.config.Config;
import devote.blockchain.api.BlockchainConfiguration;

// Class is used for testing a blockchain config with not correct package name
// Due to limitations of Mockito (not able to mock final classes) this class exists.
public class BlockchainConfigWithNotCorrectPackage implements BlockchainConfiguration {
    @Override
    public String getNetworkName() {
        return "blockchain_with_not_correct_package_name";
    }

    @Override
    public void init(Config config) {

    }
}
