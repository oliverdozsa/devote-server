package devote.blockchain.blockchainconfigwithnodefaultctr.blockchainwithmissingimplementation;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.ChannelGenerator;
import devote.blockchain.api.ChannelGeneratorAccountOperation;
import devote.blockchain.api.Account;

import java.util.List;

public class BlockchainWihMissingImplementationChannelGeneratorOperation implements ChannelGeneratorAccountOperation {
    @Override
    public void init(BlockchainConfiguration configuration) {

    }

    @Override
    public List<ChannelGenerator> create(long totalVotesCap, Account funding) {
        return null;
    }

    @Override
    public long calcNumOfAccountsNeeded(long totalVotesCap) {
        return 0L;
    }
}
