package devote.blockchain;

import com.typesafe.config.Config;
import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.IssuerAccount;
import org.reflections.Reflections;
import play.Logger;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Blockchains {
    private final Config config;
    private final Map<String, BlockchainFactory> factories = new HashMap<>();

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
        if (packageName == null || packageName.length() == 0) {
            return false;
        }

        if (!packageName.startsWith("devote.blockchain")) {
            return false;
        }

        String[] parts = packageName.split("\\.");
        return parts.length == 3;
    }

    private BlockchainFactory assembleFactory(Class<? extends BlockchainConfiguration> blockchainConfigClass) {
        BlockchainConfiguration blockchainConfiguration;
        try {
            blockchainConfiguration = blockchainConfigClass.getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            logger.warn("assembleFactory(): Failed to create config instance!", e);
            return null;
        }

        String packageName = blockchainConfigClass.getPackage().getName();
        Reflections blockchainReflections = new Reflections(packageName);

        Class<? extends IssuerAccount> issuerAccountClass =
                findUniqueClassOfOrNull(IssuerAccount.class, blockchainReflections);
        if (issuerAccountClass == null) {
            return null;
        }

        blockchainConfiguration.init(config.withOnlyPath(packageName));
        return new BlockchainFactory(blockchainConfiguration, issuerAccountClass);
    }

    private static <T> Class<? extends T> findUniqueClassOfOrNull(
            Class<T> classToFind,
            Reflections blockchainReflections
    ) {
        Set<Class<? extends T>> classes = blockchainReflections.getSubTypesOf(classToFind);
        if (classes == null || classes.size() != 1) {
            logger.warn("findUniqueClassOfOrNull(): Could not find unique subtype of class: {}", classToFind.getName());
            return null;
        } else {
            return classes.iterator().next();
        }
    }

    private void putIfNotAlreadyThere(BlockchainFactory factory) {
        if(factories.containsKey(factory.getNetworkName())) {
            logger.warn("putIfNotAlreadyThere(): Already have a factory for network {}", factory.getNetworkName());
        } else {
            factories.put(factory.getNetworkName(), factory);
        }
    }
}
