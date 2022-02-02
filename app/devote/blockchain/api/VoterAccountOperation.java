package devote.blockchain.api;

public interface VoterAccountOperation extends BlockchainOperation {
    String createTransaction(CreationData creationData);

    class CreationData {
        public KeyPair channelKeyPair;
        public KeyPair distributionKeyPair;
        public Issuer issuer;
        public String voterPublicKey;
    }
}
