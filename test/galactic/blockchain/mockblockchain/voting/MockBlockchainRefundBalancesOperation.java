package galactic.blockchain.mockblockchain.voting;

import galactic.blockchain.api.Account;
import galactic.blockchain.api.BlockchainConfiguration;
import galactic.blockchain.api.RefundBalancesOperation;

import java.util.List;

public class MockBlockchainRefundBalancesOperation implements RefundBalancesOperation {
    @Override
    public void init(BlockchainConfiguration configuration) {

    }

    @Override
    public void useTestNet() {

    }

    @Override
    public int maxNumOfAccountsToRefundInOneTransaction() {
        return 50;
    }

    @Override
    public void refundBalancesWithPayment(Account destination, List<Account> accountsToRefund) {

    }

    @Override
    public void refundBalancesWithTermination(Account destination, List<Account> accountsToTerminate) {

    }
}
