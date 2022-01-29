package devote.blockchain.api;

public interface ChannelAccountOperation extends BlockchainOperation {
    int maxNumOfAccountsToCreateInOneBatch();

    KeyPair create(long votesCap, KeyPair issuerKeyPair);
}
