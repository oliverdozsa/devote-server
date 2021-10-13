package devote.blockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.IssuerAccount;
import play.Logger;

public class BlockchainFactory {
    private final String networkName;
    private final BlockchainConfiguration configuration;
    private final Class<? extends IssuerAccount> issuerAccountClass;

    private static final Logger.ALogger logger = Logger.of(BlockchainFactory.class);

    public BlockchainFactory(BlockchainConfiguration configuration, Class<? extends IssuerAccount> issuerAccountClass) {
        this.configuration = configuration;
        this.issuerAccountClass = issuerAccountClass;
        this.networkName = configuration.getNetworkName();
    }

    public IssuerAccount createIssuerAccount() {
        try {
            IssuerAccount issuerAccount = issuerAccountClass
                    .getDeclaredConstructor()
                    .newInstance();

            issuerAccount.init(configuration);

            return issuerAccount;
        } catch (Exception e) {
            logger.error("createIssuerAccount(): failed to create IssuerAccount!", e);
            throw new BlockchainException("Failed to create IssuerAccount!", e);
        }
    }

    public String getNetworkName() {
        return networkName;
    }
}
