package galactic.blockchain.api;

import java.util.List;

public interface ChannelAccountOperation extends BlockchainOperation {
    int maxNumOfAccountsToCreateInOneBatch();

    List<Account> create(ChannelGenerator channelGenerator, int numOfAccountsToCreate);
}
