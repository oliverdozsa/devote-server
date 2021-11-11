package devote.blockchain;

import com.typesafe.config.Config;
import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainOperation;
import org.reflections.Reflections;
import play.Logger;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static devote.blockchain.BlockchainUtils.findUniqueSubtypeOfOrNull;

public class Blockchains {
    private final Config config;
    private final Map<String, BlockchainFactory> factories = new HashMap<>();

    private static final Set<Class<? extends BlockchainOperation>> requiredImplementationInterfaces =
            collectRequiredImplementationInterfaces();

    private static final Logger.ALogger logger = Logger.of(Blockchains.class);

    @Inject
    public Blockchains(Config config) {
        logger.info("Started discovery of supported blockchains");
        this.config = config;

        Reflections rootReflections = new Reflections("devote.blockchain");
        Set<Class<? extends BlockchainConfiguration>> foundBlockchainConfigs = rootReflections
                .getSubTypesOf(BlockchainConfiguration.class);

        foundBlockchainConfigs.stream()
                .map(this::createFactory)
                .filter(Objects::nonNull)
                .forEach(this::putIfNotAlreadyThere);

        logger.info("Found supported blockchains: {}", factories.keySet());
    }

    public BlockchainFactory getFactoryByNetwork(String networkName) {
        return factories.get(networkName);
    }

    private BlockchainFactory createFactory(Class<? extends BlockchainConfiguration> blockchainConfigClass) {
        String packageName = blockchainConfigClass.getPackage().getName();
        if (isBlockchainConfigPackageValid(packageName)) {
            return assembleFactory(blockchainConfigClass);
        } else {
            logger.warn("createFactory(): cannot create blockchain factory; invalid config package!" +
                    " blockchainConfigClass = {}, packageName = {}", blockchainConfigClass.getName(), packageName);
            return null;
        }
    }

    private static boolean isBlockchainConfigPackageValid(String packageName) {
        String[] parts = packageName.split("\\.");
        return parts.length == 3;
    }

    private BlockchainFactory assembleFactory(Class<? extends BlockchainConfiguration> blockchainConfigClass) {
        BlockchainConfiguration blockchainConfiguration = createConfigInstance(blockchainConfigClass);
        if(blockchainConfiguration == null) {
            return null;
        }

        return createBlockchainFactory(blockchainConfiguration);
    }

    private static BlockchainConfiguration createConfigInstance(Class<? extends BlockchainConfiguration> blockchainConfigClass) {
        try {
            return blockchainConfigClass.getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            logger.warn("createConfigInstance(): Failed to create config instance!", e);
            return null;
        }
    }

    private BlockchainFactory createBlockchainFactory(BlockchainConfiguration blockchainConfiguration) {
        String packageName = blockchainConfiguration.getClass().getPackage().getName();
        Reflections blockchainReflections = new Reflections(packageName);

        if(doAllRequiredImplementationsExist(blockchainReflections)) {
            blockchainConfiguration.init(config.withOnlyPath(packageName));
            return new BlockchainFactory(blockchainConfiguration, blockchainReflections);
        } else {
            logger.warn("createBlockchainFactory(): Could not find one or more required implementation classes in package: {}", packageName);
            return null;
        }
    }

    private static boolean doAllRequiredImplementationsExist(Reflections blockchainReflections) {
        for (Class<? extends BlockchainOperation> requiredImplementationInterface : requiredImplementationInterfaces) {
            if (findUniqueSubtypeOfOrNull(requiredImplementationInterface, blockchainReflections) == null) {
                return false;
            }
        }

        return true;
    }

    private void putIfNotAlreadyThere(BlockchainFactory factory) {
        if (factories.containsKey(factory.getNetworkName())) {
            logger.warn("putIfNotAlreadyThere(): Already have a factory for network {}", factory.getNetworkName());
        } else {
            factories.put(factory.getNetworkName(), factory);
        }
    }

    private static Set<Class<? extends BlockchainOperation>> collectRequiredImplementationInterfaces() {
        Reflections apiReflections = new Reflections("devote.blockchain.api");
        return apiReflections.getSubTypesOf(BlockchainOperation.class);
    }
}
