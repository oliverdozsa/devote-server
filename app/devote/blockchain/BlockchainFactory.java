package devote.blockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.BlockchainOperation;
import devote.blockchain.api.ChannelAccountOperation;
import devote.blockchain.api.DistributionAndBallotAccountOperation;
import devote.blockchain.api.IssuerAccountOperation;
import devote.blockchain.api.VoterAccountOperation;
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

    public IssuerAccountOperation createIssuerAccountOperation() {
        return createBlockchainOperation(IssuerAccountOperation.class);
    }

    public ChannelAccountOperation createChannelAccountOperation() {
        return createBlockchainOperation(ChannelAccountOperation.class);
    }

    public DistributionAndBallotAccountOperation createDistributionAndBallotAccountOperation() {
        return createBlockchainOperation(DistributionAndBallotAccountOperation.class);
    }

    public VoterAccountOperation createVoterAccountOperation() {
        return createBlockchainOperation(VoterAccountOperation.class);
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
