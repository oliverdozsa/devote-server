package galactic.blockchain.blockchainconfigwithnodefaultctr.blockchainwithmissingimplementation;

import com.typesafe.config.Config;
import galactic.blockchain.api.BlockchainConfiguration;

public class BlockchainWithMissingImplementation implements BlockchainConfiguration {
    @Override
    public String getNetworkName() {
        return "blockchain_with_missing_implementation";
    }

    @Override
    public void init(Config config) {

    }
}
