package devote.blockchain.api;

public interface FundingAccountOperation extends BlockchainOperation {
    boolean doesAccountHaveAtLeastBalanceOf(String publicKey, String amount);
}
