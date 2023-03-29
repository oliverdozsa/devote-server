package services;

import data.entities.JpaVoting;
import data.entities.Visibility;
import data.operations.VoterDbOperations;
import data.operations.VotingDbOperations;
import galactic.blockchain.api.ChannelGenerator;
import galactic.blockchain.operations.VotingBlockchainOperations;
import exceptions.ForbiddenException;
import play.Logger;
import requests.CreateVotingRequest;
import responses.VotingResponse;
import responses.VotingResponseFromJpaVoting;
import security.VerifiedJwt;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;


public class VotingService {
    private final VotingDbOperations votingDbOperations;
    private final VoterDbOperations voterDbOperations;
    private final VotingBlockchainOperations votingBlockchainOperations;
    private final VotingResponseFromJpaVoting votingResponseFromJpaVoting;

    private static final Logger.ALogger logger = Logger.of(VotingService.class);

    @Inject
    public VotingService(
            VotingDbOperations votingDbOperations,
            VotingBlockchainOperations votingBlockchainOperations,
            VoterDbOperations voterDbOperations
    ) {
        this.votingDbOperations = votingDbOperations;
        this.votingBlockchainOperations = votingBlockchainOperations;
        this.voterDbOperations = voterDbOperations;
        votingResponseFromJpaVoting = new VotingResponseFromJpaVoting();
    }

    public CompletionStage<String> create(CreateVotingRequest request, VerifiedJwt jwt) {
        logger.info("create(): request = {}", request);
        CreatedVotingData createdVotingData = new CreatedVotingData();

        return checkIfUserIsAllowedToCreateVoting(jwt)
                .thenCompose(v -> votingBlockchainOperations.checkFundingAccountOf(request))
                .thenCompose(v -> votingDbOperations.initialize(request, jwt.getUserId()))
                .thenAccept(createdVotingData::setId)
                .thenApply(v -> createdVotingData.encodedId);
    }

    public CompletionStage<VotingResponse> single(String id) {
        logger.info("single(): id = {}", id);

        return Base62Conversions.decodeAsStage(id)
                .thenCompose(votingDbOperations::single)
                .thenCompose(this::checkIfUnauthenticatedUserAllowedToViewSingleVote)
                .thenApply(votingResponseFromJpaVoting::convert);
    }

    public CompletionStage<VotingResponse> single(String id, VerifiedJwt jwt) {
        return Base62Conversions.decodeAsStage(id)
                .thenCompose(votingDbOperations::single)
                .thenCompose(voting -> checkIfUserIsAllowedToViewSingleVote(voting, jwt))
                .thenApply(votingResponseFromJpaVoting::convert);
    }

    private CompletionStage<JpaVoting> checkIfUnauthenticatedUserAllowedToViewSingleVote(JpaVoting voting) {
        return supplyAsync(() -> {
            if(voting.getVisibility() == Visibility.PRIVATE) {
                String message = String.format("Voting %s is private, unauthenticated user is not allowed to view it!", voting.getId());
                logger.warn("checkIfUnauthenticatedUserAllowedToViewSingleVote(): {}", message);
                throw new ForbiddenException(message);
            }

            return voting;
        });
    }

    private CompletionStage<Void> checkIfUserIsAllowedToCreateVoting(VerifiedJwt jwt) {
        return runAsync(() -> {
            if (!jwt.hasVoteCallerRole()) {
                String message = String.format("User %s is not allowed to create voting.", jwt.getUserId());
                logger.warn("checkIfUserIsAllowedToCreateVoting(): {}", message);
                throw new ForbiddenException(message);
            }
        });
    }

    private CompletionStage<JpaVoting> checkIfUserIsAllowedToViewSingleVote(JpaVoting voting, VerifiedJwt jwt) {
        CompletionStage<Boolean> participationCheckStage = voterDbOperations.doesParticipateInVoting(jwt.getUserId(), voting.getId());
        CompletionStage<JpaVoting> justTheVoteStage = supplyAsync(() -> voting);

        if (voting.getVisibility() != Visibility.PRIVATE) {
            logger.info("checkIfUserIsAllowedToViewSingleVote(): Voting {} is not private, user {} is allowed to view",
                    voting.getId(), jwt.getUserId());
            return justTheVoteStage;
        } else if(!jwt.hasVoterRole() && !jwt.hasVoteCallerRole()) {
            String message = String.format("User %s has no proper role; not allowed to view voting %s", jwt.getUserId(), voting.getId());
            logger.warn("checkIfUserIsAllowedToViewSingleVote(): {}", message);
            return supplyAsync(() -> {
                throw new ForbiddenException(message);
            });
        } else if (voting.getCreatedBy().equals(jwt.getUserId())) {
            return justTheVoteStage;
        } else {
            return participationCheckStage
                    .thenAccept(doesParticipate -> evaluateParticipation(doesParticipate, jwt.getUserId(), voting.getId()))
                    .thenCompose(v -> justTheVoteStage);
        }
    }

    private void evaluateParticipation(boolean doesParticipate, String userId, Long votingId) {
        if (!doesParticipate) {
            String message = String.format("Voting %s is private, and user %s is not caller, and not participant!",
                    votingId, userId);
            logger.warn("checkParticipation(): {}", message);
            throw new ForbiddenException(message);
        }
    }

    private static class CreatedVotingData {
        public Long id;
        public List<ChannelGenerator> channelGenerators;
        public String encodedId;

        public void setId(Long id) {
            this.id = id;
            encodedId = Base62Conversions.encode(id);
        }
    }
}
