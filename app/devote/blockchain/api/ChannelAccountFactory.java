package devote.blockchain.api;

public interface ChannelAccountFactory extends BlockchainOperation {
    int maxNumOfAccountsToCreateInOneBatch();

    KeyPair create(long votesCap, KeyPair issuerKeyPair);
}
