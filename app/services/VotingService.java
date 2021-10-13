package services;

import com.typesafe.config.Config;
import play.Logger;

import javax.inject.Inject;

public class VotingService {
    private final Config config;

    private static final Logger.ALogger logger = Logger.of(VotingService.class);

    @Inject
    public VotingService(Config config) {
        this.config = config;
    }

    public Long create() {
        // TODO
        return 42L;
    }
}
