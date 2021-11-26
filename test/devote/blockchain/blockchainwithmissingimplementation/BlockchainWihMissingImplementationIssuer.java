package devote.blockchain.blockchainwithmissingimplementation;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.IssuerAccount;

public class BlockchainWihMissingImplementationIssuer implements IssuerAccount {
    @Override
    public void init(BlockchainConfiguration configuration) {

    }

    @Override
    public String create(long votesCap) {
        return "42";
    }

    @Override
    public int calcNumOfAccountsNeeded(long votesCap) {
        return 0;
    }

    @Override
    public String toPublicAccountId(String secret) {
        return "";
    }
}
