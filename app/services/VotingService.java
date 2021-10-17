package services;

import com.typesafe.config.Config;
import data.entities.JpaVoting;
import data.operations.VotingDbOperations;
import dto.CreateVotingRequest;
import dto.SingleVotingResponse;
import play.Logger;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class VotingService {
    private final Config config;
    private final VotingDbOperations votingDbOperations;

    private static final Logger.ALogger logger = Logger.of(VotingService.class);

    @Inject
    public VotingService(Config config, VotingDbOperations votingDbOperations) {
        this.config = config;
        this.votingDbOperations = votingDbOperations;
    }

    public CompletionStage<Long> create(CreateVotingRequest request) {
        logger.info("create(): request = {}", request);
        return votingDbOperations.initialize(request);
    }

    public CompletionStage<SingleVotingResponse> single(Long id) {
        logger.info("single(): id = {}", id);
        return votingDbOperations.single(id)
                .thenApply(VotingService::toSingleVotingResponse);
    }

    private static SingleVotingResponse toSingleVotingResponse(JpaVoting entity) {
        SingleVotingResponse votingResponse = new SingleVotingResponse();
        votingResponse.setId(entity.getId());
        votingResponse.setNetwork(entity.getNetwork());
        return votingResponse;
    }
}
