package devote.blockchain.api;

public interface ChannelAccount extends BlockchainOperation {
    int maxNumOfAccountsToCreateInOneBatch();

    KeyPair create(long votesCap, String issuerSecret);
}
