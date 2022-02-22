package devote.blockchain.stellar;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.ChannelAccountOperation;
import devote.blockchain.api.Account;
import devote.blockchain.api.ChannelGenerator;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.CreateAccountOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import play.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static devote.blockchain.stellar.StellarUtils.fromAccount;
import static devote.blockchain.stellar.StellarUtils.toAccount;

public class StellarChannelAccountOperation implements ChannelAccountOperation {
    private StellarServerAndNetwork serverAndNetwork;
    private StellarBlockchainConfiguration configuration;

    private static final int MAX_NUM_OF_ACCOUNTS_TO_CREATE_IN_ONE_TRANSACTION = 50;
    private static final String STARTING_BALANCE_STR = "2";

    private static final Logger.ALogger logger = Logger.of(StellarChannelAccountOperation.class);

    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
        serverAndNetwork = StellarServerAndNetwork.create(this.configuration);
    }

    @Override
    public void useTestNet() {
        serverAndNetwork = StellarServerAndNetwork.createForTestNet(configuration);
    }

    @Override
    public int maxNumOfAccountsToCreateInOneBatch() {
        return MAX_NUM_OF_ACCOUNTS_TO_CREATE_IN_ONE_TRANSACTION;
    }

    @Override
    public List<Account> create(ChannelGenerator channelGenerator, int numOfAccountsToCreate) {
        try {
            KeyPair channelGeneratorKeyPair = fromAccount(channelGenerator.account);

            Transaction.Builder txBuilder = prepareTransaction(channelGeneratorKeyPair.getAccountId());
            List<KeyPair> channels = prepareChannelsCreationOn(txBuilder, numOfAccountsToCreate);

            submitTransaction(txBuilder, channelGeneratorKeyPair);

            return channels.stream()
                    .map(StellarUtils::toAccount)
                    .collect(Collectors.toList());
        } catch (IOException | AccountRequiresMemoException e) {
            logger.warn("[STELLAR]: Failed to create channel account!", e);
            throw new BlockchainException("[STELLAR]: Failed to create channel account!", e);
        }
    }

    private Transaction.Builder prepareTransaction(String channelGeneratorAccountId) throws IOException {
        Server server = serverAndNetwork.getServer();
        Network network = serverAndNetwork.getNetwork();

        return StellarUtils.createTransactionBuilder(server, network, channelGeneratorAccountId);
    }

    private List<KeyPair> prepareChannelsCreationOn(Transaction.Builder txBuilder, int numOfAccountsToCreate) {
        List<KeyPair> newAccounts = generateNewAccounts(numOfAccountsToCreate);

        logger.info("[STELLAR]: Attempting to create {} channel accounts with starting balance: {}",
                numOfAccountsToCreate, STARTING_BALANCE_STR);
        newAccounts.forEach(newAccount -> prepareAccountCreationOn(txBuilder, newAccount));

        return newAccounts;
    }

    private List<KeyPair> generateNewAccounts(int numOfAccountsToCreate) {
        List<KeyPair> newAccounts = new ArrayList<>();
        for (int i = 0; i < numOfAccountsToCreate; i++) {
            newAccounts.add(KeyPair.random());
        }

        return newAccounts;
    }

    private void prepareAccountCreationOn(Transaction.Builder txBuilder, KeyPair account) {
        CreateAccountOperation createAccount = new CreateAccountOperation.Builder(account.getAccountId(), STARTING_BALANCE_STR)
                .build();
        txBuilder.addOperation(createAccount);
    }

    private void submitTransaction(Transaction.Builder txBuilder, KeyPair channel) throws AccountRequiresMemoException, IOException {
        Transaction transaction = txBuilder.build();
        transaction.sign(channel);

        Server server = serverAndNetwork.getServer();
        StellarSubmitTransaction.submit("channel account", transaction, server);
    }
}
