package devote.blockchain;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.BlockchainOperation;
import devote.blockchain.api.ChannelAccountFactory;
import devote.blockchain.api.DistributionAndBallotAccountFactory;
import devote.blockchain.api.IssuerAccountFactory;
import devote.blockchain.api.VoterAccountFactory;
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

    public IssuerAccountFactory createIssuerAccount() {
        return createBlockchainOperation(IssuerAccountFactory.class);
    }

    public ChannelAccountFactory createChannelAccount() {
        return createBlockchainOperation(ChannelAccountFactory.class);
    }

    public DistributionAndBallotAccountFactory createDistributionAndBallotAccount() {
        return createBlockchainOperation(DistributionAndBallotAccountFactory.class);
    }

    public VoterAccountFactory createVoterAccount() {
        return createBlockchainOperation(VoterAccountFactory.class);
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
