package galactic.blockchain.stellar;

import org.stellar.sdk.Network;
import org.stellar.sdk.Server;

public class StellarServerAndNetwork {
    private final StellarBlockchainConfiguration configuration;
    private final boolean shouldUseTestNet;

    public static StellarServerAndNetwork create(StellarBlockchainConfiguration configuration) {
        return new StellarServerAndNetwork(configuration, false);
    }

    public static StellarServerAndNetwork createForTestNet(StellarBlockchainConfiguration configuration) {
        return new StellarServerAndNetwork(configuration, true);
    }

    public Network getNetwork() {
        if (shouldUseTestNet) {
            return configuration.getTestNetwork();
        }

        return configuration.getNetwork();
    }

    public Server getServer() {
        if (shouldUseTestNet) {
            return configuration.getTestNetServer();
        }

        return configuration.getServer();
    }

    public StellarServerAndNetwork(StellarBlockchainConfiguration configuration, boolean shouldUseTestNet) {
        this.configuration = configuration;
        this.shouldUseTestNet = shouldUseTestNet;
    }
}
