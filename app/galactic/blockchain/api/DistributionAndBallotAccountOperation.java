package galactic.blockchain.api;


public interface DistributionAndBallotAccountOperation extends BlockchainOperation {
    TransactionResult create(Account funding, String assetCode, long votesCap);


    class TransactionResult {
        public final Account distribution;
        public final Account ballot;
        public final Account issuer;

        public TransactionResult(Account distribution, Account ballot, Account issuer) {
            this.distribution = distribution;
            this.ballot = ballot;
            this.issuer = issuer;
        }
    }
}
