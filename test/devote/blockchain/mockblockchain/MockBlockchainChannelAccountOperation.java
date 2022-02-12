package devote.blockchain.mockblockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.ChannelAccountOperation;
import devote.blockchain.api.Account;
import devote.blockchain.api.ChannelGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockBlockchainChannelAccountOperation implements ChannelAccountOperation {
    public static final int NUM_OF_CHANNEL_ACCOUNTS_TO_CREATE_IN_ON_BATCH = 11;

    private static int currentChannelAccountId = 0;

    @Override
    public void init(BlockchainConfiguration configuration) {
    }

    @Override
    public int maxNumOfAccountsToCreateInOneBatch() {
        return NUM_OF_CHANNEL_ACCOUNTS_TO_CREATE_IN_ON_BATCH;
    }

    @Override
    public List<Account> create(ChannelGenerator channelGenerator, int numOfAccountsToCreate) {
        List<Account> accounts = new ArrayList<>();
        for(int i = 0; i < numOfAccountsToCreate; i++) {
            currentChannelAccountId++;
            String currentChannelAccountIdAsString = Integer.toString(currentChannelAccountId);
            Account account = new Account(currentChannelAccountIdAsString, currentChannelAccountIdAsString);
            accounts.add(account);
        }

        return accounts;
    }

    public static boolean isCreated(String accountSecret) {
        int accountValue = Integer.parseInt(accountSecret);
        return accountValue <= currentChannelAccountId;
    }
}
