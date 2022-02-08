package devote.blockchain.api;

public interface IssuerAccountOperation extends BlockchainOperation {
    /**
     * Creates the issuer account.
     *
     * @param votesCap The votes cap for the issuer.
     * @return Keypair of the created account
     */
    KeyPair create(long votesCap);

    /**
     * Should calc the number of issuer accounts to create optionally based on votes cap.
     *
     * @param totalVotesCap Number of voters allowed.
     * @return See above
     */
    long calcNumOfAccountsNeeded(long totalVotesCap);
}
