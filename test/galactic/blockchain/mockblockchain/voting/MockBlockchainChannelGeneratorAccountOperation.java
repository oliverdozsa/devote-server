package galactic.blockchain.mockblockchain.voting;

import galactic.blockchain.api.BlockchainConfiguration;
import galactic.blockchain.api.ChannelGenerator;
import galactic.blockchain.api.ChannelGeneratorAccountOperation;
import galactic.blockchain.api.Account;
import galactic.blockchain.mockblockchain.MockBlockchainConfiguration;

import java.util.ArrayList;
import java.util.List;

public class MockBlockchainChannelGeneratorAccountOperation implements ChannelGeneratorAccountOperation {
    public static final int NUM_OF_ISSUER_ACCOUNTS_TO_CREATE = 4;

    private MockBlockchainConfiguration config;
    private boolean isInitCalled = false;
    private boolean shouldUseTestNet = false;

    private static int currentChannelGeneratorId = 0;

    @Override
    public void init(BlockchainConfiguration config) {
        this.config = (MockBlockchainConfiguration) config;
        isInitCalled = true;
    }

    @Override
    public void useTestNet() {
        shouldUseTestNet = true;
    }

    @Override
    public List<ChannelGenerator> create(long totalVotesCap, Account funding) {
        long n = calcNumOfAccountsNeeded(totalVotesCap);
        long votesCapPerAccount = totalVotesCap / n;
        long votesCapRemainder = totalVotesCap % n;

        List<ChannelGenerator> channelGenerators = new ArrayList<>();
        for (int i = 0; i < n - 1; i++) {
            currentChannelGeneratorId++;
            String currentChannelGeneratorIdAsString = Integer.toString(currentChannelGeneratorId);
            if(shouldUseTestNet) {
                currentChannelGeneratorIdAsString = "test-net-" + currentChannelGeneratorIdAsString;
            }

            Account account = new Account(currentChannelGeneratorIdAsString, currentChannelGeneratorIdAsString);
            ChannelGenerator channelGenerator = new ChannelGenerator(account, votesCapPerAccount);
            channelGenerators.add(channelGenerator);
        }

        currentChannelGeneratorId++;
        String currentChannelGeneratorIdAsString = Integer.toString(currentChannelGeneratorId);

        if(shouldUseTestNet) {
            currentChannelGeneratorIdAsString = "test-net-" + currentChannelGeneratorIdAsString;
        }

        Account account = new Account(currentChannelGeneratorIdAsString, currentChannelGeneratorIdAsString);

        ChannelGenerator channelGenerator = new ChannelGenerator(account, votesCapPerAccount + votesCapRemainder);
        channelGenerators.add(channelGenerator);

        return channelGenerators;
    }

    @Override
    public long calcNumOfAccountsNeeded(long totalVotesCap) {
        return NUM_OF_ISSUER_ACCOUNTS_TO_CREATE;
    }

    public MockBlockchainConfiguration getConfig() {
        return config;
    }

    public boolean isInitCalled() {
        return isInitCalled;
    }

    public static boolean isCreated(String account) {
        int accountValue = Integer.parseInt(account);
        return accountValue <= currentChannelGeneratorId;
    }
}
