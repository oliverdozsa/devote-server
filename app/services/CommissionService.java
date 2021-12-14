package services;

import data.entities.JpaCommissionInitSession;
import data.operations.CommissionDbOperations;
import exceptions.ForbiddenException;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import play.Logger;
import requests.CommissionInitRequest;
import responses.CommissionInitResponse;
import security.JwtMaker;
import security.VerifiedJwt;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletionStage;

import static crypto.RsaKeyUtils.publicKeyToPemString;

public class CommissionService {
    private static final Logger.ALogger logger = Logger.of(CommissionService.class);

    private final AsymmetricCipherKeyPair envelopeKeyPair;
    private final CommissionDbOperations commissionDbOperations;
    private JwtMaker jwtMaker;

    @Inject
    public CommissionService(
            @Named("envelope") AsymmetricCipherKeyPair envelopeKeyPair,
            CommissionDbOperations commissionDbOperations,
            JwtMaker jwtMaker
    ) {
        this.envelopeKeyPair = envelopeKeyPair;
        this.commissionDbOperations = commissionDbOperations;
        this.jwtMaker = jwtMaker;
    }

    public CompletionStage<CommissionInitResponse> init(CommissionInitRequest request, VerifiedJwt jwt) {
        logger.info("init(): request = {}, userId = ", request.toString(), jwt.getUserId());

        Long decodedVotingId = Base62Conversions.decode(request.getVotingId());
        logger.info("init(): decodedId = " + decodedVotingId);

        return checkIfUserIsAuthorizedToInitSession(decodedVotingId, jwt.getUserId())
                .thenCompose(v -> commissionDbOperations.createSession(decodedVotingId, jwt.getUserId()))
                .thenApply(this::toInitResponse);
    }

    private CompletionStage<Void> checkIfUserIsAuthorizedToInitSession(Long votingId, String userId) {
        logger.info("checkIfUserIsAuthorizedToInitSession()");
        return commissionDbOperations.doesSessionExistForUserInVoting(votingId, userId)
                .thenAccept(doesExist -> {
                    if (doesExist) {
                        throw new ForbiddenException("User " + userId + " has already started a session in voting " + votingId);
                    }
                });
    }

    private CommissionInitResponse toInitResponse(JpaCommissionInitSession entity) {
        CommissionInitResponse initResponse = new CommissionInitResponse();

        String sessionJwt = jwtMaker.create(entity.getVoting().getId(), entity.getUserId());
        initResponse.setSessionToken(sessionJwt);
        initResponse.setPublicKey(publicKeyToPemString(envelopeKeyPair));

        return initResponse;
    }
}
