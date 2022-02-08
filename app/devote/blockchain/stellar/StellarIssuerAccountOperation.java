package devote.blockchain.stellar;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.IssuerAccountOperation;
import devote.blockchain.api.KeyPair;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.CreateAccountOperation;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import play.Logger;
import utils.StringUtils;

import java.io.IOException;

public class StellarIssuerAccountOperation implements IssuerAccountOperation {
    private StellarBlockchainConfiguration configuration;

    private static final Logger.ALogger logger = Logger.of(StellarIssuerAccountOperation.class);


    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
    }

    @Override
    public KeyPair create(long votesCap) {
        try {
            Transaction.Builder txBuilder = prepareTransaction();
            org.stellar.sdk.KeyPair issuerKeyPair = prepareIssuerCreationOn(txBuilder, votesCap);
            submitTransaction(txBuilder);

            return toDevoteKeyPair(issuerKeyPair);
        } catch (IOException | AccountRequiresMemoException e) {
            logger.warn("[STELLAR]: Failed to create issuer account!", e);
            throw new BlockchainException("[STELLAR]: Failed to create issuer account!", e);
        }
    }

    @Override
    public long calcNumOfAccountsNeeded(long totalVotesCap) {
        return configuration.getNumOfVoteBuckets();
    }

    private Transaction.Builder prepareTransaction() throws IOException {
        Server server = configuration.getServer();
        Network network = configuration.getNetwork();
        org.stellar.sdk.KeyPair masterKeyPair = configuration.getMasterKeyPair();

        return StellarUtils.createTransactionBuilder(server, network, masterKeyPair.getAccountId());
    }

    private org.stellar.sdk.KeyPair prepareIssuerCreationOn(Transaction.Builder txBuilder, long votesCapForIssuer) {
        org.stellar.sdk.KeyPair issuerKeyPair = org.stellar.sdk.KeyPair.random();
        String startingBalance = calcStartingBalanceFor(votesCapForIssuer);
        CreateAccountOperation createAccountOperation =
                new CreateAccountOperation.Builder(issuerKeyPair.getAccountId(), startingBalance)
                        .build();
        txBuilder.addOperation(createAccountOperation);

        logger.info("[STELLAR]: About to create issuer account: {} with starting balance: {}",
                StringUtils.redactWithEllipsis(issuerKeyPair.getAccountId(), 5),
                startingBalance);

        return issuerKeyPair;
    }

    private String calcStartingBalanceFor(long votesCapPerIssuer) {
        return Long.toString((4 * votesCapPerIssuer) + 10);
    }

    private void submitTransaction(Transaction.Builder txBuilder) throws AccountRequiresMemoException, IOException {
        Server server = configuration.getServer();
        org.stellar.sdk.KeyPair masterKeyPair = configuration.getMasterKeyPair();

        Transaction transaction = txBuilder.build();
        transaction.sign(masterKeyPair);

        StellarSubmitTransaction.submit(transaction, server);
    }

    private static KeyPair toDevoteKeyPair(org.stellar.sdk.KeyPair stellarKeyPair) {
        return new KeyPair(new String(stellarKeyPair.getSecretSeed()), stellarKeyPair.getAccountId());
    }
}
