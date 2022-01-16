package services.commissionsubs;

import data.entities.JpaCommissionSession;
import data.operations.CommissionDbOperations;
import exceptions.ForbiddenException;
import play.Logger;
import requests.CommissionInitRequest;
import responses.CommissionInitResponse;
import security.JwtCenter;
import security.VerifiedJwt;
import services.Base62Conversions;

import java.util.concurrent.CompletionStage;

public class CommissionInitSubService {
    private final String envelopePublicKeyPem;
    private final JwtCenter jwtCenter;
    private final CommissionDbOperations commissionDbOperations;

    private static final Logger.ALogger logger = Logger.of(CommissionInitSubService.class);

    public CommissionInitSubService(String envelopePublicKeyPem, JwtCenter jwtCenter, CommissionDbOperations commissionDbOperations) {
        this.envelopePublicKeyPem = envelopePublicKeyPem;
        this.jwtCenter = jwtCenter;
        this.commissionDbOperations = commissionDbOperations;
    }

    public CompletionStage<CommissionInitResponse> init(CommissionInitRequest request, VerifiedJwt jwt) {
        logger.info("init(): request = {}, userId = {}", request.toString(), jwt.getUserId());
        return Base62Conversions.decodeAsStage(request.getVotingId())
                .thenCompose(decodedVotingId -> checkIfUserIsAuthorizedToInitSession(decodedVotingId, jwt.getUserId()))
                .thenCompose(votingId -> commissionDbOperations.createSession(votingId, jwt.getUserId()))
                .thenApply(this::toInitResponse);
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

    private CommissionInitResponse toInitResponse(JpaCommissionSession entity) {
        CommissionInitResponse initResponse = new CommissionInitResponse();

        String sessionJwt = jwtCenter.create(entity.getVoting().getId(), entity.getUserId());
        initResponse.setSessionJwt(sessionJwt);
        initResponse.setPublicKey(envelopePublicKeyPem);

        return initResponse;
    }
}
