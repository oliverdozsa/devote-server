package devote.blockchain.api;

public interface ChannelAccount extends BlockchainOperation {
    int maxNumOfAccountsToCreateInOneBatch();

    String create(long votesCap, String issuerSecret);
}
