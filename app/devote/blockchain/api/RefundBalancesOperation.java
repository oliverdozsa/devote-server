package devote.blockchain.api;

import java.util.List;

public interface RefundBalancesOperation extends BlockchainOperation {
    int maxNumOfAccountsToRefundInOneTransaction();
    void refundBalancesWithPayment(Account destination, List<Account> accountsToRefund);
    void refundBalancesWithTermination(Account destination, List<Account> accountsToTerminate);
}
