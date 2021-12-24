package services;

import data.operations.CommissionDbOperations;
import exceptions.BusinessLogicViolationException;
import exceptions.ForbiddenException;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.engines.RSAEngine;
import play.Logger;
import requests.CommissionSignEnvelopeRequest;
import responses.CommissionSignEnvelopeResponse;
import security.VerifiedJwt;

import java.util.Base64;
import java.util.concurrent.CompletionStage;

class CommissionSignEnvelopeSubService {
    private final AsymmetricCipherKeyPair envelopeKeyPair;
    private final CommissionDbOperations commissionDbOperations;

    private static final Logger.ALogger logger = Logger.of(CommissionSignEnvelopeSubService.class);

    public CommissionSignEnvelopeSubService(AsymmetricCipherKeyPair envelopeKeyPair, CommissionDbOperations commissionDbOperations) {
        this.envelopeKeyPair = envelopeKeyPair;
        this.commissionDbOperations = commissionDbOperations;
    }

    public CompletionStage<CommissionSignEnvelopeResponse> signEnvelope(CommissionSignEnvelopeRequest request, VerifiedJwt jwt) {
        logger.info("signEnvelope(): user: {}, voting: {}", jwt.getUserId(), jwt.getVotingId());

        if (!jwt.getVotingId().isPresent()) {
            throw new BusinessLogicViolationException("Missing voting ID in JWT! user: " + jwt.getUserId());
        }

        String userId = jwt.getUserId();
        Long votingId = jwt.getVotingId().get();

        return commissionDbOperations.hasAlreadySignedAnEnvelope(userId, votingId)
                .thenAccept(hasAlreadySigned -> forbidIfUserAlreadySignedAnEnvelope(userId, votingId, hasAlreadySigned))
                .thenCompose(v -> createAndSaveEnvelopeSignature(request, userId, votingId))
                .thenApply(this::createResponse);
    }

    private String signEnvelope(CommissionSignEnvelopeRequest request) {
        byte[] messageBytes = Base64.getDecoder().decode(request.getEnvelopeBase64());
        RSAEngine engine = new RSAEngine();
        engine.init(true, envelopeKeyPair.getPrivate());
        byte[] signatureBytes = engine.processBlock(messageBytes, 0, messageBytes.length);
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    private void forbidIfUserAlreadySignedAnEnvelope(String userId, Long votingId, Boolean hasAlreadySigned) {
        if (hasAlreadySigned) {
            String votingIdBase62 = Base62Conversions.encode(votingId);
            String message = String.format("User \"%s\" has already signed an envelope in voting \"%s\"", userId, votingIdBase62);

            logger.warn("forbidIfUserAlreadySignedAnEnvelope(): {}", message);
            throw new ForbiddenException(message);
        }
    }

    private CompletionStage<String> createAndSaveEnvelopeSignature(
            CommissionSignEnvelopeRequest request,
            String userId,
            Long votingId) {
        String envelopeSignatureBase64 = signEnvelope(request);
        return commissionDbOperations.storeEnvelopeSignature(userId, votingId, envelopeSignatureBase64);
    }

    private CommissionSignEnvelopeResponse createResponse(String signature) {
        CommissionSignEnvelopeResponse response = new CommissionSignEnvelopeResponse();
        response.setEnvelopeSignatureBase64(signature);
        return response;
    }
}
