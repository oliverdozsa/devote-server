package devote.blockchain.api;

public interface IssuerAccount extends BlockchainOperation {
    String create(long votesCap);

    int calcNumOfAccountsNeeded(long votesCap);
}
