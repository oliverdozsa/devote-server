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
    private KeyPair masterKeyPair;
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

    public long getNumOfVoteBuckets() {
        return config.getLong("devote.blockchain.stellar.votebuckets");
    }

    public KeyPair getMasterKeyPair() {
        if (masterKeyPair == null) {
            masterKeyPair = KeyPair.fromSecretSeed(config.getString("devote.blockchain.stellar.secret"));
        }

        return masterKeyPair;
    }

    private void initServerAndNetworkIfNeeded() {
        if (server == null) {
            String horizonUrl = config.getString("devote.blockchain.stellar.url");
            logger.info("[STELLAR]: horizon url = {}", horizonUrl);
            server = new Server(horizonUrl);

            if (horizonUrl.contains("testnet")) {
                network = Network.TESTNET;
            } else {
                network = Network.PUBLIC;
            }
        }
    }
}
