package devote.blockchain.api;

import com.typesafe.config.Config;

/**
 * Will be instantiated once per network. This is a place where you can place any globals if necessary.
 *
 * Blockchain implementations should be placed under devote.blockchain.[blockchain-package].
 * More precisely, the BlockchainConfiguration class should be placed under devote.blockchain.[blockchain-package]
 * and the rest of the classes could be placed anywhere under that package.
 *
 * Each implementation class must have a no-arg constructor, and there should be exactly one implementation of a
 * blockchain operation under the mentioned package.
 *
 */
public interface BlockchainConfiguration {
    /**
     * This is called first. It identifies the network.
     *
     * @return The network name
     */
    String getNetworkName();

    /**
     * After {@link BlockchainConfiguration#getNetworkName()} is called, this method will be called
     * with initial configs.
     *
     * @param config The configs belonging to this network.
     */
    void init(Config config);
}
