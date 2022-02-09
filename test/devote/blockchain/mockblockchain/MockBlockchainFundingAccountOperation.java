package devote.blockchain.mockblockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.FundingAccountOperation;

public class MockBlockchainFundingAccountOperation implements FundingAccountOperation {
    @Override
    public void init(BlockchainConfiguration configuration) {

    }

    @Override
    public boolean doesAccountNotHaveEnoughBalanceForVotesCap(String accountPublic, long votesCap) {
        return false;
    }
}
