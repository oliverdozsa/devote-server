package devote.blockchain.api;


import java.util.List;
import java.util.Map;

public interface DistributionAndBallotAccountOperation extends BlockchainOperation {
    TransactionResult create(List<Issuer> issuers);


    class TransactionResult {
        public final KeyPair distribution;
        public final KeyPair ballot;

        public TransactionResult(KeyPair distribution, KeyPair ballot) {
            this.distribution = distribution;
            this.ballot = ballot;
        }
    }
}
