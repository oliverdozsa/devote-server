package devote.blockchain.stellar;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.ChannelAccountOperation;
import devote.blockchain.api.Account;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.CreateAccountOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import play.Logger;
import utils.StringUtils;

import java.io.IOException;

import static devote.blockchain.stellar.StellarUtils.fromAccount;

public class StellarChannelAccountOperation implements ChannelAccountOperation {
    private StellarBlockchainConfiguration configuration;

    private static final int MAX_NUM_OF_ACCOUNTS_TO_CREATE_IN_ONE_TRANSACTION = 50;
    private static final String STARTING_BALANCE_STR = "2";

    private static final Logger.ALogger logger = Logger.of(StellarChannelAccountOperation.class);

    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
    }

    @Override
    public int maxNumOfAccountsToCreateInOneBatch() {
        return MAX_NUM_OF_ACCOUNTS_TO_CREATE_IN_ONE_TRANSACTION;
    }

    @Override
    public Account create(long votesCap, Account issuerAccount) {
        try {
            KeyPair issuer = fromAccount(issuerAccount);

            Transaction.Builder txBuilder = prepareTransaction(issuer.getAccountId());
            KeyPair channel = prepareChannelCreationOn(txBuilder);
            submitTransaction(txBuilder, issuer);

            return new Account(new String(channel.getSecretSeed()), channel.getAccountId());
        } catch (IOException | AccountRequiresMemoException e) {
            logger.warn("[STELLAR]: Failed to create channel account!", e);
            throw new BlockchainException("[STELLAR]: Failed to create channel account!", e);
        }
    }

    private Transaction.Builder prepareTransaction(String issuerAccountId) throws IOException {
        Server server = configuration.getServer();
        Network network = configuration.getNetwork();

        return StellarUtils.createTransactionBuilder(server, network, issuerAccountId);
    }

    private KeyPair prepareChannelCreationOn(Transaction.Builder txBuilder) {
        KeyPair channel = KeyPair.random();
        CreateAccountOperation createAccount = new CreateAccountOperation.Builder(channel.getAccountId(), STARTING_BALANCE_STR)
                .build();
        txBuilder.addOperation(createAccount);

        logger.info("[STELLAR]: Attempting to create channel account: {} with starting balance: {}",
                StringUtils.redactWithEllipsis(channel.getAccountId(), 5),
                STARTING_BALANCE_STR);

        return channel;
    }

    private void submitTransaction(Transaction.Builder txBuilder, KeyPair issuer) throws AccountRequiresMemoException, IOException {
        Transaction transaction = txBuilder.build();
        logger.info("channel envelope xdr: {}", transaction.toEnvelopeXdrBase64());

        transaction.sign(issuer);

        Server server = configuration.getServer();
        StellarSubmitTransaction.submit(transaction, server);
    }
}
