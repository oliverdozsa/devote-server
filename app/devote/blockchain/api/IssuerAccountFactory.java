package devote.blockchain.api;

public interface IssuerAccountFactory extends BlockchainOperation {
    /**
     *
     * @param votesCap The total number of voters.
     * @param i The index number (1 based) of the i-th account to be created (the last one has i = numOfAccountsNeeded).
     * @return Keypair of the created account
     */
    KeyPair create(long votesCap, int i);

    int calcNumOfAccountsNeeded(long votesCap);
}
