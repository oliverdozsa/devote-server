package devote.blockchain.blockchainconfigwithnodefaultctr.blockchainwithmissingimplementation;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.IssuerAccountOperation;
import devote.blockchain.api.KeyPair;

public class BlockchainWihMissingImplementationIssuerOperation implements IssuerAccountOperation {
    @Override
    public void init(BlockchainConfiguration configuration) {

    }

    @Override
    public KeyPair create(long votesCap) {
        return new KeyPair("42", "84");
    }

    @Override
    public int calcNumOfAccountsNeeded(long totalVotesCap) {
        return 0;
    }
}
