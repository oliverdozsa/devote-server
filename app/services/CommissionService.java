package services;

import data.operations.CommissionDbOperations;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import play.Logger;
import requests.CommissionInitRequest;
import requests.CommissionSignEnvelopeRequest;
import responses.CommissionInitResponse;
import responses.CommissionSignEnvelopeResponse;
import security.JwtCenter;
import security.VerifiedJwt;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletionStage;

import static crypto.RsaKeyUtils.publicKeyToPemString;

public class CommissionService {
    private static final Logger.ALogger logger = Logger.of(CommissionService.class);

    private final CommissionInitSubService initSubService;
    private final CommissionSignEnvelopeSubService signEnvelopeSubService;

    @Inject
    public CommissionService(
            @Named("envelope") AsymmetricCipherKeyPair envelopeKeyPair,
            CommissionDbOperations commissionDbOperations,
            JwtCenter jwtCenter
    ) {
        initSubService = new CommissionInitSubService(publicKeyToPemString(envelopeKeyPair), jwtCenter, commissionDbOperations);
        signEnvelopeSubService = new CommissionSignEnvelopeSubService(envelopeKeyPair, commissionDbOperations);
    }

    public CompletionStage<CommissionInitResponse> init(CommissionInitRequest request, VerifiedJwt jwt) {
        logger.info("init(): request = {}, userId = {}", request.toString(), jwt.getUserId());
        return initSubService.init(request, jwt);
    }

    public CompletionStage<CommissionSignEnvelopeResponse> signEnvelope(CommissionSignEnvelopeRequest request, VerifiedJwt jwt) {
        logger.info("signEnvelope(): user: {}, voting: {}", jwt.getUserId(), jwt.getVotingId());
        return signEnvelopeSubService.signEnvelope(request, jwt);
    }

    // TODO: Request account creation message: votingId|voterPublicAccountId
    //       Store signature in DB. In case the message is replayed, it can be checked whether the account
    //       has already been created or not.
}
