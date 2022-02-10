package data.repositories.imp;

import exceptions.NotFoundException;
import io.ebean.EbeanServer;

public class EbeanRepositoryUtils {
    public static <E> void assertEntityExists(EbeanServer ebean, Class<E> entityClass, Object id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null!");
        }

        if (ebean.find(entityClass, id) == null) {
            String message = String.format("No such entity %s (%s) found!", entityClass.getName(), id);
            throw new NotFoundException(message);
        }
    }

    private EbeanRepositoryUtils() {
    }
}
