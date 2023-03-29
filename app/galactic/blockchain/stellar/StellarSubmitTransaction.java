package galactic.blockchain.stellar;

import galactic.blockchain.api.BlockchainException;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.Operation;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.SubmitTransactionResponse;
import play.Logger;

import java.io.IOException;
import java.util.StringJoiner;

public class StellarSubmitTransaction {
    private static final Logger.ALogger logger = Logger.of(StellarSubmitTransaction.class);

    public static void submit(String name, Transaction transaction, Server server) throws AccountRequiresMemoException, IOException {
        logger.info("[STELLAR]: Submitting {} transaction with operations: {}", name, collectionOperationsOf(transaction));
        SubmitTransactionResponse response = server.submitTransaction(transaction);

        if (response.isSuccess()) {
            logger.info("[STELLAR]: Successfully submitted transaction!");
        } else {
            String logMessage = String.format("[STELLAR]: Failed to submit transaction! %s", StellarUtils.resultCodesOf(response));
            logger.error(logMessage);
            throw new BlockchainException(logMessage);
        }
    }

    private static String collectionOperationsOf(Transaction transaction) {
        StringJoiner joiner = new StringJoiner(", ");
        for (Operation operation : transaction.getOperations()) {
            String operationName =
                    operation.getClass().getCanonicalName()
                            .replace("Operation", "")
                            .replace("org.stellar.sdk.", "");
            joiner.add(operationName);
        }

        return joiner.toString();
    }

    private StellarSubmitTransaction() {
    }
}
