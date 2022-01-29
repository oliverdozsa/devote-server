package devote.blockchain.api;

public interface IssuerAccountOperation extends BlockchainOperation {
    /**
     * Creates the issuer account.
     *
     * @param votesCap The total number of voters.
     * @param i        The index number (1 based) of the i-th account to be created (the last one has i = numOfAccountsNeeded).
     * @return Keypair of the created account
     */
    KeyPair create(long votesCap, int i);

    /**
     * Should calc the number of issuer accounts to create optionally based on votes cap.
     *
     * @param votesCap Number of voters allowed.
     * @return See above
     */
    int calcNumOfAccountsNeeded(long votesCap);
}
