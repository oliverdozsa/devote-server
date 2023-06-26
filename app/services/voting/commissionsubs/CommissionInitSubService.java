package services.voting.commissionsubs;

import data.entities.voting.JpaCommissionSession;
import data.operations.voting.CommissionDbOperations;
import data.operations.voting.VoterDbOperations;
import exceptions.ForbiddenException;
import play.Logger;
import requests.voting.CommissionInitRequest;
import responses.voting.CommissionInitResponse;
import security.VerifiedJwt;
import services.Base62Conversions;

import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.runAsync;

public class CommissionInitSubService {
    private final String envelopePublicKeyPem;
    private final CommissionDbOperations commissionDbOperations;
    private final VoterDbOperations voterDbOperations;

    private static final Logger.ALogger logger = Logger.of(CommissionInitSubService.class);

    public CommissionInitSubService(String envelopePublicKeyPem, CommissionDbOperations commissionDbOperations, VoterDbOperations voterDbOperations) {
        this.envelopePublicKeyPem = envelopePublicKeyPem;
        this.commissionDbOperations = commissionDbOperations;
        this.voterDbOperations = voterDbOperations;
    }

    public CompletionStage<CommissionInitResponse> init(CommissionInitRequest request, VerifiedJwt jwt) {
        logger.info("init(): request = {}, userId = {}", request.toString(), jwt.getUserId());
        return Base62Conversions.decodeAsStage(request.getVotingId())
                .thenCompose(decodedVotingId -> checkIfUserIsAllowedToParticipateInVoting(decodedVotingId, jwt))
                .thenCompose(this::checkIfVotingIsInitializedProperly)
                .thenCompose(decodedVotingId -> checkIfUserIsAuthorizedToInitSession(decodedVotingId, jwt.getUserId()))
                .thenCompose(votingId -> commissionDbOperations.createSession(votingId, jwt.getUserId()))
                .thenApply(this::toInitResponse);
    }

    private CompletionStage<Long> checkIfUserIsAllowedToParticipateInVoting(Long votingId, VerifiedJwt jwt) {
        logger.info("checkIfUserIsAllowedToParticipateInVoting(): votingId = {}, userId = {}", votingId, jwt.getUserId());

        return checkVoterRole(jwt)
                .thenCompose(v -> voterDbOperations.doesParticipateInVoting(jwt.getUserId(), votingId))
                .thenCompose(doesParticipate -> checkIfDoesParticipate(doesParticipate, jwt.getUserId(), votingId))
                .thenApply(v -> votingId);
    }

    private CompletionStage<Void> checkVoterRole(VerifiedJwt jwt) {
        return runAsync(() -> {
            if(!jwt.hasVoterRole()) {
                String message = String.format("User %s has no voter role!", jwt.getUserId());
                logger.warn("checkVoterRole(): {}", message);
                throw new ForbiddenException(message);
            }
        });
    }

    private CompletionStage<Void> checkIfDoesParticipate(boolean doesParticipate, String userId, Long votingId) {
        return runAsync(() -> {
            if(!doesParticipate) {
                String message = String.format("User %s does not participate in voting %d!", userId, votingId);
                logger.warn("checkIfUserIsAllowedToParticipateInVoting(): {}", message);
                throw new ForbiddenException(message);
            }
        });
    }

    private CompletionStage<Long> checkIfUserIsAuthorizedToInitSession(Long votingId, String userId) {
        logger.info("checkIfUserIsAuthorizedToInitSession(): votingId = {}, userId = {}", votingId, userId);
        return commissionDbOperations.doesSessionExistForUserInVoting(votingId, userId)
                .thenApply(doesExist -> {
                    if (doesExist) {
                        String message = "User " + userId + " has already started a session in voting " + votingId;
                        logger.warn("checkIfUserIsAuthorizedToInitSession(): " + message);
                        throw new ForbiddenException(message);
                    } else {
                        logger.info("checkIfUserIsAuthorizedToInitSession(): User is authorized.");
                        return votingId;
                    }
                });
    }

    private CommissionInitResponse toInitResponse(JpaCommissionSession votingSession) {
        CommissionInitResponse initResponse = new CommissionInitResponse();
        initResponse.setPublicKey(envelopePublicKeyPem);

        return initResponse;
    }

    private CompletionStage<Long> checkIfVotingIsInitializedProperly(Long votingId) {
        return commissionDbOperations.isVotingInitializedProperly(votingId).thenApply(isProperlyInitialized -> {
            if (isProperlyInitialized) {
                logger.info("checkIfVotingIsInitializedProperly(): Voting is initialized properly.");
                return votingId;
            } else {
                String message = String.format("Voting %d is not initialized properly.", votingId);
                logger.warn("checkIfVotingIsInitializedProperly(): {}", message);
                throw new ForbiddenException(message);
            }
        });
    }
}
