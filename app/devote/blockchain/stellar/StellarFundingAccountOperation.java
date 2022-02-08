package devote.blockchain.stellar;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.FundingAccountOperation;

public class StellarFundingAccountOperation implements FundingAccountOperation {
    @Override
    public void init(BlockchainConfiguration configuration) {

    }

    @Override
    public boolean doesAccountHaveAtLeastBalanceOf(String publicKey, String amount) {
        // TODO
        return true;
    }
}
