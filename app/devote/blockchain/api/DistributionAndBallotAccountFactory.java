package devote.blockchain.api;


import java.util.List;
import java.util.Map;

public interface DistributionAndBallotAccountFactory extends BlockchainOperation {
    TransactionResult create(List<IssuerData> issuerData, Long votesCap);

    class IssuerData {
        public final String voteTokenTitle;
        public final KeyPair issuerKeyPair;

        public IssuerData(String voteTokenTitle, KeyPair issuerKeyPair) {
            this.voteTokenTitle = voteTokenTitle;
            this.issuerKeyPair = issuerKeyPair;
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
