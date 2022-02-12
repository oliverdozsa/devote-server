package devote.blockchain.stellar;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.IssuerAccountOperation;
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
import static devote.blockchain.stellar.StellarUtils.toAccount;

public class StellarIssuerAccountOperation implements IssuerAccountOperation {
    private StellarBlockchainConfiguration configuration;

    private static final Logger.ALogger logger = Logger.of(StellarIssuerAccountOperation.class);


    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
    }

    @Override
    public Account create(long votesCap, Account funding) {
        try {
            Transaction.Builder txBuilder = prepareTransaction(funding);
            KeyPair issuer = prepareIssuerCreationOn(txBuilder, votesCap);
            submitTransaction(txBuilder, funding);

            return toAccount(issuer);
        } catch (IOException | AccountRequiresMemoException e) {
            logger.warn("[STELLAR]: Failed to create issuer account!", e);
            throw new BlockchainException("[STELLAR]: Failed to create issuer account!", e);
        }
    }

    @Override
    public long calcNumOfAccountsNeeded(long totalVotesCap) {
        return calcNumOfAccountNeededBasedOn(configuration);
    }

    public static long calcNumOfAccountNeededBasedOn(StellarBlockchainConfiguration configuration) {
        return configuration.getNumOfVoteBuckets();
    }

    private Transaction.Builder prepareTransaction(Account funding) throws IOException {
        Server server = configuration.getServer();
        Network network = configuration.getNetwork();

        return StellarUtils.createTransactionBuilder(server, network, funding.publik);
    }

    private org.stellar.sdk.KeyPair prepareIssuerCreationOn(Transaction.Builder txBuilder, long votesCapForIssuer) {
        KeyPair issuer = KeyPair.random();
        String startingBalance = calcStartingBalanceFor(votesCapForIssuer);
        CreateAccountOperation createAccount = new CreateAccountOperation.Builder(issuer.getAccountId(), startingBalance)
                .build();
        txBuilder.addOperation(createAccount);

        logger.info("[STELLAR]: About to create issuer account: {} with starting balance: {}",
                StringUtils.redactWithEllipsis(issuer.getAccountId(), 5),
                startingBalance);

        return issuer;
    }

    private String calcStartingBalanceFor(long votesCapPerIssuer) {
        return Long.toString((2 * votesCapPerIssuer) + 10);
    }

    private void submitTransaction(Transaction.Builder txBuilder, Account fundingAccount) throws AccountRequiresMemoException, IOException {
        Transaction transaction = txBuilder.build();
        KeyPair funding = fromAccount(fundingAccount);

        transaction.sign(funding);

        Server server = configuration.getServer();
        StellarSubmitTransaction.submit(transaction, server);
    }
}
