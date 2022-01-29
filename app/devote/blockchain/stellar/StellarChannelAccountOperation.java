package devote.blockchain.stellar;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.ChannelAccountOperation;
import devote.blockchain.api.KeyPair;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.CreateAccountOperation;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import play.Logger;
import utils.StringUtils;

import java.io.IOException;

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
    public KeyPair create(long votesCap, KeyPair issuerKeyPair) {
        try {
            org.stellar.sdk.KeyPair stellarIssuerKeyPair = StellarUtils.fromDevoteKeyPair(issuerKeyPair);

            Transaction.Builder txBuilder = prepareTransaction(stellarIssuerKeyPair.getAccountId());
            org.stellar.sdk.KeyPair stellarChannelKeyPair = prepareChannelCreationOn(txBuilder);
            submitTransaction(txBuilder, stellarIssuerKeyPair);

            return new KeyPair(new String(stellarChannelKeyPair.getSecretSeed()), stellarChannelKeyPair.getAccountId());
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

    private org.stellar.sdk.KeyPair prepareChannelCreationOn(Transaction.Builder txBuilder) {
        org.stellar.sdk.KeyPair stellarChannelKeyPair = org.stellar.sdk.KeyPair.random();
        CreateAccountOperation createAccountOperation =
                new CreateAccountOperation.Builder(stellarChannelKeyPair.getAccountId(), STARTING_BALANCE_STR)
                        .build();

        txBuilder.addOperation(createAccountOperation);

        logger.info("[STELLAR]: Attempting to create channel account: {} with starting balance: {}",
                StringUtils.redactWithEllipsis(stellarChannelKeyPair.getAccountId(), 5),
                STARTING_BALANCE_STR);

        return stellarChannelKeyPair;
    }

    private void submitTransaction(Transaction.Builder txBuilder, org.stellar.sdk.KeyPair issuerKeyPair) throws AccountRequiresMemoException, IOException {
        Transaction transaction = txBuilder.build();
        transaction.sign(issuerKeyPair);

        Server server = configuration.getServer();
        server.submitTransaction(transaction);
    }
}
