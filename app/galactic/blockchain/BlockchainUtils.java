package galactic.blockchain;

import org.reflections.Reflections;
import play.Logger;

import java.util.Set;

public class BlockchainUtils {
    private static final Logger.ALogger logger = Logger.of(BlockchainUtils.class);

    public static <T> Class<? extends T> findUniqueSubtypeOfOrNull(Class<T> classToFind, Reflections reflections) {
        Set<Class<? extends T>> classes = reflections.getSubTypesOf(classToFind);
        if (classes == null || classes.size() != 1) {
            logger.warn("findUniqueClassOfOrNull(): Could not find unique implementation of required class {} under package {}",
                    classToFind.getName(),
                    getPackage(reflections)
            );

            return null;
        } else {
            return classes.iterator().next();
        }
    }

    private static String getPackage(Reflections reflections) {
        String packageString = reflections.getConfiguration().getInputsFilter().toString();
        packageString = packageString.replace("+", "");
        packageString = packageString.replace("\\", "");

        return packageString;
    }

    private BlockchainUtils() {
    }
}
