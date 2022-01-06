package devote.blockchain.api;

public interface IssuerAccount extends BlockchainOperation {
    KeyPair create(long votesCap);

    int calcNumOfAccountsNeeded(long votesCap);
}
