package smokes.fixtures;

import galactic.blockchain.api.Account;
import org.stellar.sdk.KeyPair;
import play.Logger;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static galactic.blockchain.stellar.StellarUtils.toAccount;
import static utils.StringUtils.redactWithEllipsis;

public class StellarBlockchainTestNet implements BlockchainTestNet {
    private static final String FRIENDBOT_URL = "https://friendbot.stellar.org?addr=%s";

    private static final Logger.ALogger logger = Logger.of(StellarBlockchainTestNet.class);

    private WSClient wsClient;

    public StellarBlockchainTestNet(WSClient wsClient) {
        this.wsClient = wsClient;
    }

    @Override
    public Account createAccountWithBalance(long balance) {
        KeyPair account = KeyPair.random();
        fundAccountWithBalance(account, balance);

        return toAccount(account);
    }

    private void fundAccountWithBalance(KeyPair account, long balance) {
        try {
            String loggableAccount = redactWithEllipsis(account.getAccountId(), 5);
            logger.info("[STELLAR TEST]: Creating test account {} with 10 000 XLM (requested balance is ignored).", loggableAccount);

            String fundingUrlString = String.format(FRIENDBOT_URL, account.getAccountId());
            CompletableFuture<WSResponse> responseFuture = wsClient.url(fundingUrlString)
                    .execute()
                    .toCompletableFuture();

            makeFundingRequest(responseFuture);
        } catch (Exception e) {
            throw new RuntimeException("[STELLAR TEST]: Failed to make request to create a test account", e);
        }
    }

    private void makeFundingRequest(CompletableFuture<WSResponse> responseFuture) throws ExecutionException, InterruptedException {
        WSResponse response = responseFuture.get();

        if(response.getStatus() != 200) {
            throw new RuntimeException("[STELLAR TEST]: Failed to create test account: " + response.getBody());
        } else {
            logger.info("[STELLAR TEST]: successfully created test account!");
        }
    }
}
