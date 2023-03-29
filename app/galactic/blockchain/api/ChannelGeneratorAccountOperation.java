package galactic.blockchain.api;

import java.util.List;

public interface ChannelGeneratorAccountOperation extends BlockchainOperation {
    /**
     * Creates the channel generator accounts.
     *
     * @param totalVotesCap The votes cap.
     * @param funding  The funding account.
     * @return Keypair of the created account
     */
    List<ChannelGenerator> create(long totalVotesCap, Account funding);

    /**
     * Should calc the number of channel generator accounts to create optionally based on votes cap.
     *
     * @param totalVotesCap Number of voters allowed.
     * @return See above
     */
    long calcNumOfAccountsNeeded(long totalVotesCap);
}
