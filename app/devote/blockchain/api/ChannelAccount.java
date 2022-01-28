package devote.blockchain.api;

// TODO: rename to factory
public interface ChannelAccount extends BlockchainOperation {
    int maxNumOfAccountsToCreateInOneBatch();

    KeyPair create(long votesCap, KeyPair issuerKeyPair);
}
