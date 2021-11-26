package devote.blockchain.api;

public interface IssuerAccount extends BlockchainOperation {
    String create(long votesCap);
    String toPublicAccountId(String secret);

    int calcNumOfAccountsNeeded(long votesCap);
}
