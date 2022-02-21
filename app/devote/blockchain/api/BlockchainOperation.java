package devote.blockchain.api;

public interface BlockchainOperation {
    /**
     * All blockchain operation gets a config through their init method called.
     * This is the base interface; all more concrete operation interfaces extend this.
     *
     * @param configuration The blockchain configuration.
     */
    void init(BlockchainConfiguration configuration);


    /**
     * Prepares the operation to be executed on the test network.
     */
    void useTestNet();
}
