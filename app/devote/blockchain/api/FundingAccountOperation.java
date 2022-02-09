package devote.blockchain.api;

public interface FundingAccountOperation extends BlockchainOperation {
    boolean doesAccountNotHaveEnoughBalanceForVotesCap(String accountPublic, long votesCap);
}
