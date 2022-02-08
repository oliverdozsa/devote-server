package devote.blockchain.api;


import java.util.List;

public interface DistributionAndBallotAccountOperation extends BlockchainOperation {
    TransactionResult create(List<Issuer> issuers);


    class TransactionResult {
        public final Account distribution;
        public final Account ballot;

        public TransactionResult(Account distribution, Account ballot) {
            this.distribution = distribution;
            this.ballot = ballot;
        }
    }
}
