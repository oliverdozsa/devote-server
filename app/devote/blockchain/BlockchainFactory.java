package devote.blockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.BlockchainOperation;
import devote.blockchain.api.ChannelAccount;
import devote.blockchain.api.DistributionAndBallotAccount;
import devote.blockchain.api.IssuerAccount;
import org.reflections.Reflections;
import play.Logger;

import static devote.blockchain.BlockchainUtils.findUniqueSubtypeOfOrNull;

public class BlockchainFactory {
    private final String networkName;
    private final BlockchainConfiguration configuration;
    private final Reflections blockchainReflections;

    private static final Logger.ALogger logger = Logger.of(BlockchainFactory.class);

    public BlockchainFactory(BlockchainConfiguration configuration, Reflections blockchainReflections) {
        this.configuration = configuration;
        this.blockchainReflections = blockchainReflections;
        this.networkName = configuration.getNetworkName();
    }

    public IssuerAccount createIssuerAccount() {
        return createBlockchainOperation(IssuerAccount.class);
    }

    public ChannelAccount createChannelAccount() {
        return createBlockchainOperation(ChannelAccount.class);
    }

    public DistributionAndBallotAccount createDistributionAndBallotAccount() {
        return createBlockchainOperation(DistributionAndBallotAccount.class);
    }

    private <T extends BlockchainOperation> T createBlockchainOperation(Class<T> blockChainOperationParentClass) {
        Class<? extends T> implementationClass = findUniqueSubtypeOfOrNull(blockChainOperationParentClass, blockchainReflections);

        try {
            T blockchainOperation = implementationClass
                    .getDeclaredConstructor()
                    .newInstance();

            blockchainOperation.init(configuration);

            return blockchainOperation;
        } catch (Exception e) {
            logger.error("createIssuerAccount(): failed to create instance of " + implementationClass.getName() + "; {}!", e);
            throw new BlockchainException("Failed to create instance of " + implementationClass.getName(), e);
        }
    }

    public String getNetworkName() {
        return networkName;
    }
}
