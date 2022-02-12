package devote.blockchain.api;

public interface VoterAccountOperation extends BlockchainOperation {
    String createTransaction(CreateTransactionParams params);

    class CreateTransactionParams {
        public Account channel;
        public Account distribution;
        public String issuerAccountPublic;
        public String assetCode;
        public long votesCap;
        public String voterAccountPublic;
    }
}
