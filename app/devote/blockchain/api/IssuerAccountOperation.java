package devote.blockchain.api;

public interface IssuerAccountOperation extends BlockchainOperation {
    /**
     * Creates the issuer account.
     *
     * @param votesCapForIssuer The votes cap for the issuer.
     * @return Keypair of the created account
     */
    KeyPair create(long votesCapForIssuer);

    /**
     * Should calc the number of issuer accounts to create optionally based on votes cap.
     *
     * @param votesCap Number of voters allowed.
     * @return See above
     */
    int calcNumOfAccountsNeeded(long votesCap);
}
