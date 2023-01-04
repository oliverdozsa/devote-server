package devote.blockchain.mockblockchain;

import devote.blockchain.api.Account;
import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.FundingAccountOperation;
import org.checkerframework.checker.units.qual.A;

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
    public Account createAndFundInternalFrom(Account userGivenFunding) {
        return new Account("internalFundingSecret", "internalFundingPublic");
    }
}
