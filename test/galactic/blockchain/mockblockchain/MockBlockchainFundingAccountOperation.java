package galactic.blockchain.mockblockchain;

import galactic.blockchain.api.Account;
import galactic.blockchain.api.BlockchainConfiguration;
import galactic.blockchain.api.FundingAccountOperation;

public class MockBlockchainFundingAccountOperation implements FundingAccountOperation {
    @Override
    public void init(BlockchainConfiguration configuration) {

    }

    @Override
    public void useTestNet() {

    }

    @Override
    public boolean doesNotHaveEnoughBalanceForVotesCap(String accountPublic, long votesCap) {
        return false;
    }

    @Override
    public Account createAndFundInternalFrom(Account userGivenFunding, long votesCap) {
        return new Account("internalFundingSecret", "internalFundingPublic");
    }
}
