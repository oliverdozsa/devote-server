package devote.blockchain.api;


import java.util.List;
import java.util.Map;

public interface DistributionAndBallotAccount extends BlockchainOperation {
    TransactionResult create(List<IssuerData> issuerData, Long votesCap);
    String toPublicBallotAccountId(String ballotSecret);
    String toPublicDistributionAccountId(String distributionSecret);

    class IssuerData {
        public final String voteTokenTitle;
        public final String issuerSecret;

        public IssuerData(String voteTokenTitle, String issuerSecret) {
            this.voteTokenTitle = voteTokenTitle;
            this.issuerSecret = issuerSecret;
        }
    }

    class TransactionResult {
        public final String distributionSecret;
        public final String ballotSecret;
        public final Map<String, String> issuerTokens;

        public TransactionResult(String distributionSecret, String ballotSecret, Map<String, String> issuerTokens) {
            this.distributionSecret = distributionSecret;
            this.ballotSecret = ballotSecret;
            this.issuerTokens = issuerTokens;
        }
    }
}
