package devote.blockchain.api;


import java.util.List;

public interface DistributionAndBallotAccount extends BlockchainOperation {
    TransactionResult create(List<IssuerData> issuerData, Long votesCap);

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

        public TransactionResult(String distributionSecret, String ballotSecret) {
            this.distributionSecret = distributionSecret;
            this.ballotSecret = ballotSecret;
        }
    }
}
