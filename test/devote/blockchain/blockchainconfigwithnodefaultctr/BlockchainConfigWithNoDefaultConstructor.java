package devote.blockchain.blockchainconfigwithnodefaultctr;

import com.typesafe.config.Config;
import devote.blockchain.api.BlockchainConfiguration;

public class BlockchainConfigWithNoDefaultConstructor implements BlockchainConfiguration {
    public BlockchainConfigWithNoDefaultConstructor(int a) {
    }

    @Override
    public String getNetworkName() {
        return "blockchain_with_no_default_constructor";
    }

    @Override
    public void init(Config config) {

    }
}
