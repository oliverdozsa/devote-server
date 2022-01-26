package devote.blockchain.stellar;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.IssuerAccount;
import devote.blockchain.api.KeyPair;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.CreateAccountOperation;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import play.Logger;
import utils.StringUtils;

import java.io.IOException;

public class StellarIssuerAccount implements IssuerAccount {
    private StellarBlockchainConfiguration configuration;

    private static final Logger.ALogger logger = Logger.of(StellarIssuerAccount.class);


    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
    }

    @Override
    public KeyPair create(long votesCap, int i) {
        try {
            Transaction.Builder txBuilder = prepareTransaction();
            org.stellar.sdk.KeyPair issuerKeyPair = prepareIssuerCreationOn(txBuilder, i, votesCap);
            submitTransaction(txBuilder);

            return toDevoteKeyPair(issuerKeyPair);
        } catch (IOException | AccountRequiresMemoException e) {
            logger.warn("[STELLAR]: Failed to create issuer account!", e);
            throw new BlockchainException("[STELLAR]: Failed to create issuer account!", e);
        }
    }

    @Override
    public int calcNumOfAccountsNeeded(long votesCap) {
        return configuration.getNumOfVoteBuckets();
    }

    private Transaction.Builder prepareTransaction() throws IOException {
        Server server = configuration.getServer();
        Network network = configuration.getNetwork();
        org.stellar.sdk.KeyPair masterKeyPair = configuration.getMasterKeyPair();

        return StellarUtils.createTransactionBuilder(server, network, masterKeyPair.getAccountId());
    }

    private org.stellar.sdk.KeyPair prepareIssuerCreationOn(Transaction.Builder txBuilder, int i, long votesCap) {
        org.stellar.sdk.KeyPair issuerKeyPair = org.stellar.sdk.KeyPair.random();
        String startingBalance = calcStartingBalanceFor(i, votesCap);
        CreateAccountOperation createAccountOperation =
                new CreateAccountOperation.Builder(issuerKeyPair.getAccountId(), startingBalance)
                        .build();
        txBuilder.addOperation(createAccountOperation);

        logger.info("[STELLAR]: About to create issuer account: {} with starting balance: {}",
                StringUtils.redactWithEllipsis(issuerKeyPair.getAccountId(), 5),
                startingBalance);

        return issuerKeyPair;
    }

    private String calcStartingBalanceFor(int i, long votesCap) {
        int n = calcNumOfAccountsNeeded(votesCap);

        long votesCapPerIssuer = votesCap / n;
        long votesCapForLastIssuer = votesCapPerIssuer + votesCap % n;

        long votesCapToUse = votesCapPerIssuer;
        if (i == n) {
            votesCapToUse = votesCapForLastIssuer;
        }

        long startingBalance = 4 * votesCapToUse + 10;
        return Long.toBinaryString(startingBalance);
    }

    private void submitTransaction(Transaction.Builder txBuilder) throws AccountRequiresMemoException, IOException {
        Server server = configuration.getServer();
        org.stellar.sdk.KeyPair masterKeyPair = configuration.getMasterKeyPair();

        Transaction transaction = txBuilder.build();
        transaction.sign(masterKeyPair);

        server.submitTransaction(transaction);
    }

    private static KeyPair toDevoteKeyPair(org.stellar.sdk.KeyPair stellarKeyPair) {
        return new KeyPair(new String(stellarKeyPair.getSecretSeed()), stellarKeyPair.getAccountId());
    }
}
