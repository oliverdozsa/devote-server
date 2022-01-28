package devote.blockchain.api;

// TODO: rename to factory
public interface VoterAccount extends BlockchainOperation {
    String createTransaction(CreationData creationData);

    class CreationData {
        public String channelSecret;
        public KeyPair distributionKeyPair;
        public String voterPublicKey;
        public String issuerPublicKey;
        public String assetCode;
        public Long votesCap;

        @Override
        public String toString() {
            return "CreationData{" +
                    "voterPublicKey='" + voterPublicKey + '\'' +
                    ", assetCode='" + assetCode + '\'' +
                    '}';
        }
    }
}
