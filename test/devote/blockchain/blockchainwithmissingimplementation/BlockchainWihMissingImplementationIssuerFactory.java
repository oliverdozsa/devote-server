package devote.blockchain.blockchainwithmissingimplementation;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.IssuerAccountFactory;
import devote.blockchain.api.KeyPair;

public class BlockchainWihMissingImplementationIssuerFactory implements IssuerAccountFactory {
    @Override
    public void init(BlockchainConfiguration configuration) {

    }

    @Override
    public KeyPair create(long votesCap, int i) {
        return new KeyPair("42", "84");
    }

    @Override
    public int calcNumOfAccountsNeeded(long votesCap) {
        return 0;
    }
}
