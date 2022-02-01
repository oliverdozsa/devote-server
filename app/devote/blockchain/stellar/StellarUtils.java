package devote.blockchain.stellar;

import devote.blockchain.api.DistributionAndBallotAccountOperation;
import org.stellar.sdk.Asset;
import org.stellar.sdk.ChangeTrustAsset;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.stellar.sdk.AbstractTransaction.MIN_BASE_FEE;

class StellarUtils {
    public static final int STELLAR_MIN_BASE_FEE = MIN_BASE_FEE * 3;
    public static final int STELLAR_TIMEOUT_SECONDS = 30;

    public static String resultCodesOf(SubmitTransactionResponse response) {
        List<String> operationResultCodes = response.getExtras().getResultCodes().getOperationsResultCodes();
        if(operationResultCodes == null) {
            operationResultCodes = Collections.emptyList();
        }

        String transactionResultCode = response.getExtras().getResultCodes().getTransactionResultCode();
        return "TX: " +  transactionResultCode + ", OP: " + String.join(", ", operationResultCodes);
    }

    public static Transaction.Builder createTransactionBuilder(Server server, Network network, String accountId)
            throws IOException {
        AccountResponse accountResponse = server.accounts().account(accountId);
        return new Transaction.Builder(accountResponse, network)
                .setBaseFee(STELLAR_MIN_BASE_FEE)
                .setTimeout(STELLAR_TIMEOUT_SECONDS);
    }

    public static KeyPair fromDevoteKeyPair(devote.blockchain.api.KeyPair devoteKeyPair) {
        return KeyPair.fromSecretSeed(devoteKeyPair.secretKey);
    }

    public static devote.blockchain.api.KeyPair toDevoteKeyPair(KeyPair keyPair) {
        return new devote.blockchain.api.KeyPair(new String(keyPair.getSecretSeed()), keyPair.getAccountId());
    }
}