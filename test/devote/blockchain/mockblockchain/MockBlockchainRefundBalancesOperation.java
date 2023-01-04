package devote.blockchain.mockblockchain;

import devote.blockchain.api.Account;
import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.RefundBalancesOperation;

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
