package services;

import com.typesafe.config.Config;
import data.operations.PrepareVotingInDb;
import dto.CreateVotingRequest;
import play.Logger;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class VotingService {
    private final Config config;
    private final PrepareVotingInDb prepareVotingInDb;

    private static final Logger.ALogger logger = Logger.of(VotingService.class);

    @Inject
    public VotingService(Config config, PrepareVotingInDb prepareVotingInDb) {
        this.config = config;
        this.prepareVotingInDb = prepareVotingInDb;
    }

    public CompletionStage<Long> create(CreateVotingRequest request) {
        return prepareVotingInDb.initialize(request);
    }
}
