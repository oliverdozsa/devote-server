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
    private int numOfVoteBuckets;
    private KeyPair masterKeyPair;

    private static final Logger.ALogger logger = Logger.of(StellarBlockchainConfiguration.class);


    @Override
    public String getNetworkName() {
        return "stellar";
    }

    @Override
    public void init(Config config) {
        logger.info("[STELLAR]: initializing");

        initServerAndNetwork(config);
        numOfVoteBuckets = config.getInt("devote.vote.buckets");
        masterKeyPair = KeyPair.fromSecretSeed(config.getString("devote.stellar.secret"));
    }

    public Server getServer() {
        return server;
    }

    public Network getNetwork() {
        return network;
    }

    public int getNumOfVoteBuckets() {
        return numOfVoteBuckets;
    }

    public KeyPair getMasterKeyPair() {
        return masterKeyPair;
    }

    private void initServerAndNetwork(Config config) {
        String horizonUrl = config.getString("devote.stellar.url");

        logger.info("[STELLAR]: horizon url = {}", horizonUrl);
        server = new Server(horizonUrl);

        if (horizonUrl.contains("testnet")) {
            network = Network.TESTNET;
        } else {
            network = Network.PUBLIC;
        }
    }
}
