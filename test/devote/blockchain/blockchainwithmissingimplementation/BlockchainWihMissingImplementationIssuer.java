package devote.blockchain.blockchainwithmissingimplementation;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.IssuerAccount;
import devote.blockchain.api.KeyPair;

public class BlockchainWihMissingImplementationIssuer implements IssuerAccount {
    @Override
    public void init(BlockchainConfiguration configuration) {

    }

    @Override
    public KeyPair create(long votesCap) {
        return new KeyPair("42", "84");
    }

    @Override
    public int calcNumOfAccountsNeeded(long votesCap) {
        return 0;
    }
}
