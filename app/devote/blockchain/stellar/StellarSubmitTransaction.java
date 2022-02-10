package devote.blockchain.stellar;

import devote.blockchain.api.BlockchainException;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.SubmitTransactionResponse;
import play.Logger;

import java.io.IOException;

public class StellarSubmitTransaction {
    private static final Logger.ALogger logger = Logger.of(StellarSubmitTransaction.class);

    public static void submit(Transaction transaction, Server server) throws AccountRequiresMemoException, IOException {
        logger.info("[STELLAR]: Submitting transaction...");
        SubmitTransactionResponse response = server.submitTransaction(transaction);

        if(response.isSuccess()) {
            logger.info("[STELLAR]: Successfully submitted transaction!");
        } else {
            String logMessage = String.format("[STELLAR]: Failed to submit transaction! response code: %s", StellarUtils.resultCodesOf(response));
            logger.warn(logMessage);
            throw new BlockchainException(logMessage);
        }
    }

    private StellarSubmitTransaction() {
    }
}
