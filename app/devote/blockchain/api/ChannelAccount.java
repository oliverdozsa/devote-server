package devote.blockchain.api;

public interface ChannelAccount extends BlockchainOperation {
    int numOfAccountsToCreateInOneBatch();

    String create(long votesCap, String issuerSecret);
}
