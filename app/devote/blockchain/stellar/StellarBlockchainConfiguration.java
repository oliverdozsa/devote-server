package devote.blockchain.stellar;

import com.typesafe.config.Config;
import devote.blockchain.api.BlockchainConfiguration;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import play.Logger;

public class StellarBlockchainConfiguration implements BlockchainConfiguration {
    private Server server;
    private Network network;
    private Server testNetServer;
    private Network testNetwork;

    private Config config;

    private static final Logger.ALogger logger = Logger.of(StellarBlockchainConfiguration.class);


    @Override
    public String getNetworkName() {
        return "stellar";
    }

    @Override
    public void init(Config config) {
        logger.info("[STELLAR]: initializing");
        this.config = config;
    }

    public Server getServer() {
        initServerAndNetworkIfNeeded();
        return server;
    }

    public Network getNetwork() {
        initServerAndNetworkIfNeeded();
        return network;
    }

    public Server getTestNetServer() {
        initServerAndNetworkIfNeeded();
        return testNetServer;
    }

    public Network getTestNetwork() {
        initServerAndNetworkIfNeeded();
        return testNetwork;
    }

    public long getNumOfVoteBuckets() {
        return config.getLong("devote.blockchain.stellar.votebuckets");
    }

    private void initServerAndNetworkIfNeeded() {
        if (server == null) {
            String horizonUrl = config.getString("devote.blockchain.stellar.url");
            String horizonTestNetUrl = config.getString("devote.blockchain.stellar.testnet.url");

            logger.info("[STELLAR]: horizon url = {}", horizonUrl);
            logger.info("[STELLAR]: horizon testnet url = {}", horizonTestNetUrl);

            server = new Server(horizonUrl);
            testNetServer = new Server(horizonTestNetUrl);

            network = Network.PUBLIC;
            testNetwork = Network.TESTNET;
        }
    }
}
