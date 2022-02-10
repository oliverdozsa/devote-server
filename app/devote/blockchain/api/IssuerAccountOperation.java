package devote.blockchain.api;

public interface IssuerAccountOperation extends BlockchainOperation {
    /**
     * Creates the issuer account.
     *
     * @param votesCap The votes cap for the issuer.
     * @param funding  The funding account.
     * @return Keypair of the created account
     */
    Account create(long votesCap, Account funding);

    /**
     * Should calc the number of issuer accounts to create optionally based on votes cap.
     *
     * @param totalVotesCap Number of voters allowed.
     * @return See above
     */
    long calcNumOfAccountsNeeded(long totalVotesCap);
}
