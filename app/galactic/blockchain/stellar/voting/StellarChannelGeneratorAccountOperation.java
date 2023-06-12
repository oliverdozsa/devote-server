package galactic.blockchain.stellar.voting;

import galactic.blockchain.api.BlockchainConfiguration;
import galactic.blockchain.api.BlockchainException;
import galactic.blockchain.api.ChannelGenerator;
import galactic.blockchain.api.ChannelGeneratorAccountOperation;
import galactic.blockchain.api.Account;
import galactic.blockchain.stellar.StellarBlockchainConfiguration;
import galactic.blockchain.stellar.StellarServerAndNetwork;
import galactic.blockchain.stellar.StellarSubmitTransaction;
import galactic.blockchain.stellar.StellarUtils;
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

import static galactic.blockchain.stellar.StellarUtils.fromAccount;
import static galactic.blockchain.stellar.StellarUtils.toAccount;

public class StellarChannelGeneratorAccountOperation implements ChannelGeneratorAccountOperation {
    private StellarBlockchainConfiguration configuration;
    private StellarServerAndNetwork serverAndNetwork;

    private static final Logger.ALogger logger = Logger.of(StellarChannelGeneratorAccountOperation.class);


    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
        serverAndNetwork = StellarServerAndNetwork.create(this.configuration);
    }

    @Override
    public void useTestNet() {
        serverAndNetwork = StellarServerAndNetwork.createForTestNet(this.configuration);
    }

    @Override
    public List<ChannelGenerator> create(long totalVotesCap, Account funding) {
        try {
            long numOfAccountsNeeded = calcNumOfAccountsNeeded(totalVotesCap);
            long votesCapPerChannelGen = totalVotesCap / numOfAccountsNeeded;
            long votesCapRemainder = totalVotesCap % numOfAccountsNeeded;

            List<ChannelGenerator> channelGenerators = new ArrayList<>();

            Transaction.Builder txBuilder = prepareTransaction(funding);
            for(int i = 0; i < numOfAccountsNeeded - 1; i++) {
                KeyPair channelGenKeyPair = prepareAccountCreationOn(txBuilder, votesCapPerChannelGen);
                channelGenerators.add(toChannelGenerator(channelGenKeyPair, votesCapPerChannelGen));
            }

            KeyPair channelGenKeyPair = prepareAccountCreationOn(txBuilder, votesCapPerChannelGen + votesCapRemainder);
            channelGenerators.add(toChannelGenerator(channelGenKeyPair, votesCapPerChannelGen + votesCapRemainder));

            submitTransaction(txBuilder, funding);

            return channelGenerators;
        } catch (IOException | AccountRequiresMemoException e) {
            logger.warn("[STELLAR]: Failed to create channel generator accounts!", e);
            throw new BlockchainException("[STELLAR]: Failed to create channel generator accounts!", e);
        }
    }

    @Override
    public long calcNumOfAccountsNeeded(long totalVotesCap) {
        return calcNumOfAccountsNeededBasedOn(configuration);
    }

    public static long calcNumOfAccountsNeededBasedOn(StellarBlockchainConfiguration configuration) {
        return configuration.getNumOfVoteBuckets();
    }

    private Transaction.Builder prepareTransaction(Account funding) throws IOException {
        Server server = serverAndNetwork.getServer();
        Network network = serverAndNetwork.getNetwork();

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

        Server server = serverAndNetwork.getServer();
        StellarSubmitTransaction.submit("channel generator", transaction, server);
    }

    private ChannelGenerator toChannelGenerator(KeyPair keyPair, long votesCapPerAccount) {
        return new ChannelGenerator(toAccount(keyPair), votesCapPerAccount);
    }
}
