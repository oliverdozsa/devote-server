package devote.blockchain.stellar;

import devote.blockchain.api.Account;
import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.RefundBalancesOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.ErrorResponse;
import play.Logger;

import java.io.IOException;
import java.util.List;

public class StellarRefundBalancesOperation implements RefundBalancesOperation {
    private StellarBlockchainConfiguration configuration;
    private StellarServerAndNetwork serverAndNetwork;

    private static final Logger.ALogger logger = Logger.of(StellarRefundBalancesOperation.class);

    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
        serverAndNetwork = StellarServerAndNetwork.create((StellarBlockchainConfiguration) configuration);
    }

    @Override
    public void useTestNet() {
        serverAndNetwork = StellarServerAndNetwork.createForTestNet((StellarBlockchainConfiguration) configuration);
    }

    @Override
    public int maxNumOfAccountsToRefundInOneTransaction() {
        return 15;
    }

    @Override
    public void refundBalancesWithPayment(Account destination, List<Account> accountsToRefund) {
        try {
            if (doesAccountNotExist(destination.publik)) {
                logger.warn("[STELLAR]: Refunding with payment cannot be made due to destination doesn't exist. Assuming " +
                        "accounts as refunded.");
                return;
            }

            StellarRefundWithPaymentOperation refundWithPayment = new StellarRefundWithPaymentOperation(serverAndNetwork);
            refundWithPayment.refund(destination, accountsToRefund);
        } catch (Exception e) {
            logger.warn("[STELLAR]: Failed to refund balances with payment!", e);
            throw new BlockchainException("Failed to refund balances with payment!", e);
        }
    }

    @Override
    public void refundBalancesWithTermination(Account destination, List<Account> accountsToTerminate) {
        try {
            if (doesAccountNotExist(destination.publik)) {
                logger.warn("[STELLAR]: Refunding with termination cannot be made due to destination doesn't exist. Assuming " +
                        "accounts as refunded.");
                return;
            }

            StellarRefundWithTerminationOperation refundWithTermination = new StellarRefundWithTerminationOperation(serverAndNetwork);
            refundWithTermination.refund(destination, accountsToTerminate);
        } catch (Exception e) {
            logger.warn("[STELLAR]: Failed to refund balances with termination!", e);
            throw new BlockchainException("Failed to refund balances with termination!", e);
        }
    }

    private boolean doesAccountNotExist(String accountId) {
        Server server = serverAndNetwork.getServer();

        try {
            server.accounts().account(accountId);
        } catch (ErrorResponse e) {
            if (e.getCode() == 404) {
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}
