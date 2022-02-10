package devote.blockchain.blockchainconfigwithnodefaultctr.blockchainwithmissingimplementation;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.IssuerAccountOperation;
import devote.blockchain.api.Account;

public class BlockchainWihMissingImplementationIssuerOperation implements IssuerAccountOperation {
    @Override
    public void init(BlockchainConfiguration configuration) {

    }

    @Override
    public Account create(long votesCap, Account funding) {
        return new Account("42", "84");
    }

    @Override
    public long calcNumOfAccountsNeeded(long totalVotesCap) {
        return 0L;
    }
}
