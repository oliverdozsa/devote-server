package services;

import com.typesafe.config.Config;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import play.Logger;
import requests.CommissionInitRequest;
import responses.CommissionInitResponse;
import security.VerifiedJwt;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletionStage;

import static crypto.RsaKeyUtils.publicKeyToPemString;
import static java.util.concurrent.CompletableFuture.runAsync;

public class CommissionService {
    private static final Logger.ALogger logger = Logger.of(CommissionService.class);

    private final AsymmetricCipherKeyPair envelopeKeyPair;

    @Inject
    public CommissionService(@Named("envelope") AsymmetricCipherKeyPair envelopeKeyPair) {
        this.envelopeKeyPair = envelopeKeyPair;
    }

    public CompletionStage<CommissionInitResponse> init(CommissionInitRequest request, VerifiedJwt jwt) {
        logger.info("init(): request = {}, userId = ", request.toString(), jwt.getUserId());

        Long decodedVotingId = Base62Conversions.decode(request.getVotingId());
        logger.info("init(): decodedId = " + decodedVotingId);

        return checkIfUserIsAuthorizedToInitSession(decodedVotingId, jwt.getUserId())
                .thenCompose(v -> createSession(decodedVotingId, jwt.getUserId()));

        // TODO: Get voting by id
        // TODO: Determine user id, and whether user is authorized to vote
        // TODO: Create session
        // TODO: Create session token
        // return null;
    }


    private CompletionStage<Void> checkIfUserIsAuthorizedToInitSession(Long votingId, String userId) {
        // TODO: Store sessions in DB, and check whether a session exists for the voting and that user
        //       has not initialized before.
        // TODO: if not authorized, throw exception (ForbiddenException)
        return runAsync(() -> {});
    }

    private CompletionStage<CommissionInitResponse> createSession(Long votingId, String userId) {
        logger.info("createSession(): public: " + publicKeyToPemString(envelopeKeyPair));

        // TODO: Also add session token to response
        return null;
    }


}
