package services;

import data.entities.JpaCommissionInitSession;
import data.operations.CommissionDbOperations;
import exceptions.ForbiddenException;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import play.Logger;
import requests.CommissionInitRequest;
import responses.CommissionInitResponse;
import security.JwtCenter;
import security.VerifiedJwt;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletionStage;

import static crypto.RsaKeyUtils.publicKeyToPemString;

public class CommissionService {
    private static final Logger.ALogger logger = Logger.of(CommissionService.class);

    private final AsymmetricCipherKeyPair envelopeKeyPair;
    private final String envelopePublicKeyPem;
    private final CommissionDbOperations commissionDbOperations;
    private JwtCenter jwtCenter;

    @Inject
    public CommissionService(
            @Named("envelope") AsymmetricCipherKeyPair envelopeKeyPair,
            CommissionDbOperations commissionDbOperations,
            JwtCenter jwtCenter
    ) {
        this.envelopeKeyPair = envelopeKeyPair;
        envelopePublicKeyPem = publicKeyToPemString(envelopeKeyPair);
        this.commissionDbOperations = commissionDbOperations;
        this.jwtCenter = jwtCenter;
    }

    public CompletionStage<CommissionInitResponse> init(CommissionInitRequest request, VerifiedJwt jwt) {
        logger.info("init(): request = {}, userId = {}", request.toString(), jwt.getUserId());

        Long decodedVotingId = Base62Conversions.decode(request.getVotingId());
        logger.info("init(): decodedId = {}", decodedVotingId);

        return checkIfUserIsAuthorizedToInitSession(decodedVotingId, jwt.getUserId())
                .thenCompose(v -> commissionDbOperations.createSession(decodedVotingId, jwt.getUserId()))
                .thenApply(this::toInitResponse);
    }

    private CompletionStage<Void> checkIfUserIsAuthorizedToInitSession(Long votingId, String userId) {
        logger.info("checkIfUserIsAuthorizedToInitSession(): votingId = {}, userId = {}", votingId, userId);
        return commissionDbOperations.doesSessionExistForUserInVoting(votingId, userId)
                .thenAccept(doesExist -> {
                    if (doesExist) {
                        String message = "User " + userId + " has already started a session in voting " + votingId;
                        logger.warn("checkIfUserIsAuthorizedToInitSession(): " + message);
                        throw new ForbiddenException(message);
                    } else {
                        logger.info("checkIfUserIsAuthorizedToInitSession(): User is authorized.");
                    }
                });
    }

    private CommissionInitResponse toInitResponse(JpaCommissionInitSession entity) {
        CommissionInitResponse initResponse = new CommissionInitResponse();

        String sessionJwt = jwtCenter.create(entity.getVoting().getId(), entity.getUserId());
        initResponse.setSessionToken(sessionJwt);
        initResponse.setPublicKey(envelopePublicKeyPem);

        return initResponse;
    }
}
