package services;

import data.entities.JpaCommissionInitSession;
import data.operations.CommissionDbOperations;
import exceptions.BusinessLogicViolationException;
import exceptions.ForbiddenException;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.engines.RSAEngine;
import play.Logger;
import requests.CommissionInitRequest;
import requests.CommissionSignEnvelopeRequest;
import responses.CommissionInitResponse;
import responses.CommissionSignEnvelopeResponse;
import security.JwtCenter;
import security.VerifiedJwt;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Base64;
import java.util.concurrent.CompletionStage;

import static crypto.RsaKeyUtils.publicKeyToPemString;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

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
        return Base62Conversions.decodeAsStage(request.getVotingId())
                .thenCompose(decodedVotingId -> checkIfUserIsAuthorizedToInitSession(decodedVotingId, jwt.getUserId()))
                .thenCompose(votingId -> commissionDbOperations.createSession(votingId, jwt.getUserId()))
                .thenApply(this::toInitResponse);
    }

    public CompletionStage<CommissionSignEnvelopeResponse> signEnvelope(CommissionSignEnvelopeRequest request, VerifiedJwt jwt) {
        logger.info("signEnvelope(): user: {}, voting: {}", jwt.getUserId(), jwt.getVotingId());

        if(jwt.getVotingId().isEmpty()){
            // Voting ID in JWT is only present, if the user has not already voted.
            throw new BusinessLogicViolationException("Missing voting ID in JWT! user: " + jwt.getUserId());
        }

        // TODO: Mark in DB, that user got the signature on the envelope. If user comes again with an envelope
        //       for the same voting, reject it.


        String envelopeSignatureBase64 = signEnvelope(request);
        CommissionSignEnvelopeResponse response = new CommissionSignEnvelopeResponse();
        response.setEnvelopeSignatureBase64(envelopeSignatureBase64);

        return completedFuture(response);
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

    private CommissionInitResponse toInitResponse(JpaCommissionInitSession entity) {
        CommissionInitResponse initResponse = new CommissionInitResponse();

        String sessionJwt = jwtCenter.create(entity.getVoting().getId(), entity.getUserId());
        initResponse.setSessionJwt(sessionJwt);
        initResponse.setPublicKey(envelopePublicKeyPem);

        return initResponse;
    }

    private String signEnvelope(CommissionSignEnvelopeRequest request) {
        byte[] messageBytes = Base64.getDecoder().decode(request.getEnvelopeBase64());
        RSAEngine engine = new RSAEngine();
        engine.init(true, envelopeKeyPair.getPrivate());
        byte[] signatureBytes = engine.processBlock(messageBytes, 0, messageBytes.length);
        return Base64.getEncoder().encodeToString(signatureBytes);
    }
}
