package devote.blockchain.mockblockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.IssuerAccountOperation;
import devote.blockchain.api.Account;

public class MockBlockchainIssuerAccountOperation implements IssuerAccountOperation {
    public static final int NUM_OF_ISSUER_ACCOUNTS_TO_CREATE = 4;

    private MockBlockchainConfiguration config;
    private boolean isInitCalled = false;

    private static int currentIssuerAccountId = 0;

    @Override
    public void init(BlockchainConfiguration config) {
        this.config = (MockBlockchainConfiguration) config;
        isInitCalled = true;
    }

    @Override
    public Account create(long votesCap, Account funding) {
        currentIssuerAccountId++;
        String currentIssuerAccountIdAsString = Integer.toString(currentIssuerAccountId);
        return new Account(currentIssuerAccountIdAsString, currentIssuerAccountIdAsString);
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
        return accountValue <= currentIssuerAccountId;
    }
}
