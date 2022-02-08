package devote.blockchain.api;

public interface ChannelAccountOperation extends BlockchainOperation {
    int maxNumOfAccountsToCreateInOneBatch();

    Account create(long votesCap, Account issuerAccount);
}
