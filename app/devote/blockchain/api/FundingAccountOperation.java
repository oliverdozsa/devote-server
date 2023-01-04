package devote.blockchain.api;

public interface FundingAccountOperation extends BlockchainOperation {
    boolean doesNotHaveEnoughBalanceForVotesCap(String accountPublic, long votesCap);
    Account createAndFundInternalFrom(Account userGivenFunding);
}
