package devote.blockchain.api;


public interface DistributionAndBallotAccount extends BlockchainOperation {
    TransactionResult create(IssuerData[] issuerData, Long votesCap);

    class IssuerData {
        public final String voteTokenTitle;
        public final String issuerSecret;

        public IssuerData(String voteTokenTitle, String issuerSecret) {
            this.voteTokenTitle = voteTokenTitle;
            this.issuerSecret = issuerSecret;
        }
    }

    class TransactionResult {
        public final String ballotSecret;
        public final String distributionSecret;

        public TransactionResult(String ballotSecret, String distributionSecret) {
            this.ballotSecret = ballotSecret;
            this.distributionSecret = distributionSecret;
        }
    }
}
