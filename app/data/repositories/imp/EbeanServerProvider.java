package data.repositories.imp;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import javax.inject.Provider;

public class EbeanServerProvider implements Provider<EbeanServer> {
    private final EbeanConfig ebeanConfig;

    @Inject
    public EbeanServerProvider(EbeanConfig ebeanConfig) {
        this.ebeanConfig = ebeanConfig;
    }

    @Override
    public EbeanServer get() {
        return Ebean.getServer(ebeanConfig.defaultServer());
    }
}
