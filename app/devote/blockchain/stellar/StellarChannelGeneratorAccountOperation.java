package devote.blockchain.stellar;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.ChannelGenerator;
import devote.blockchain.api.ChannelGeneratorAccountOperation;
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
import java.util.ArrayList;
import java.util.List;

import static devote.blockchain.stellar.StellarUtils.fromAccount;
import static devote.blockchain.stellar.StellarUtils.toAccount;

public class StellarChannelGeneratorAccountOperation implements ChannelGeneratorAccountOperation {
    private StellarBlockchainConfiguration configuration;

    private static final Logger.ALogger logger = Logger.of(StellarChannelGeneratorAccountOperation.class);


    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
    }

    @Override
    public List<ChannelGenerator> create(long totalVotesCap, Account funding) {
        try {
            long numOfAccountsNeeded = calcNumOfAccountsNeeded(totalVotesCap);
            long votesCapPerIssuer = totalVotesCap / numOfAccountsNeeded;
            long votesCapRemainder = totalVotesCap % numOfAccountsNeeded;

            List<ChannelGenerator> channelGenerators = new ArrayList<>();

            Transaction.Builder txBuilder = prepareTransaction(funding);
            for(int i = 0; i < numOfAccountsNeeded - 1; i++) {
                KeyPair channelGenKeyPair = prepareAccountCreationOn(txBuilder, votesCapPerIssuer);
                channelGenerators.add(toChannelGenerator(channelGenKeyPair, votesCapPerIssuer));
            }

            KeyPair channelGenKeyPair = prepareAccountCreationOn(txBuilder, votesCapPerIssuer + votesCapRemainder);
            channelGenerators.add(toChannelGenerator(channelGenKeyPair, votesCapPerIssuer + votesCapRemainder));

            submitTransaction(txBuilder, funding);

            return channelGenerators;
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

    private KeyPair prepareAccountCreationOn(Transaction.Builder txBuilder, long votesCapPerAccount) {
        KeyPair newAccount = KeyPair.random();
        String startingBalance = calcStartingBalanceFor(votesCapPerAccount);
        CreateAccountOperation createAccount = new CreateAccountOperation.Builder(newAccount.getAccountId(), startingBalance)
                .build();
        txBuilder.addOperation(createAccount);

        logger.info("[STELLAR]: About to create channel generator account: {} with starting balance: {}",
                StringUtils.redactWithEllipsis(newAccount.getAccountId(), 5),
                startingBalance
        );

        return newAccount;
    }

    private String calcStartingBalanceFor(long votesCapPerAccount) {
        return Long.toString((2 * votesCapPerAccount) + 10);
    }

    private void submitTransaction(Transaction.Builder txBuilder, Account fundingAccount) throws AccountRequiresMemoException, IOException {
        Transaction transaction = txBuilder.build();
        KeyPair funding = fromAccount(fundingAccount);

        transaction.sign(funding);

        Server server = configuration.getServer();
        StellarSubmitTransaction.submit(transaction, server);
    }

    private ChannelGenerator toChannelGenerator(KeyPair keyPair, long votesCapPerAccount) {
        return new ChannelGenerator(toAccount(keyPair), votesCapPerAccount);
    }
}
