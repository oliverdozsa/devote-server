package devote.blockchain.api;

public interface IssuerAccount {
    void init(BlockchainConfiguration config);
    String create(long votesCap);
}
