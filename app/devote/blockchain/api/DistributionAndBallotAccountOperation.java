package devote.blockchain.api;


import java.util.List;
import java.util.Map;

public interface DistributionAndBallotAccountOperation extends BlockchainOperation {
    TransactionResult create(List<IssuerData> issuerData);

    class IssuerData {
        public final String voteTokenTitle;
        public final KeyPair keyPair;
        public final long votesCap;

        public IssuerData(String voteTokenTitle, KeyPair keyPair, long votesCap) {
            this.voteTokenTitle = voteTokenTitle;
            this.keyPair = keyPair;
            this.votesCap = votesCap;
        }
    }

    class TransactionResult {
        public final KeyPair distribution;
        public final KeyPair ballot;
        public final Map<String, String> issuerTokens;

        public TransactionResult(KeyPair distribution, KeyPair ballot, Map<String, String> issuerTokens) {
            this.distribution = distribution;
            this.ballot = ballot;
            this.issuerTokens = issuerTokens;
        }
    }
}
