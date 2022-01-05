package services;

import data.operations.CommissionDbOperations;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import play.Logger;
import requests.CommissionAccountCreationRequest;
import requests.CommissionInitRequest;
import requests.CommissionSignEnvelopeRequest;
import responses.CommissionAccountCreationResponse;
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
    private final CommissionCreateAccountSubService createAccountSubService;

    @Inject
    public CommissionService(
            @Named("envelope") AsymmetricCipherKeyPair envelopeKeyPair,
            CommissionDbOperations commissionDbOperations,
            JwtCenter jwtCenter
    ) {
        initSubService = new CommissionInitSubService(publicKeyToPemString(envelopeKeyPair), jwtCenter, commissionDbOperations);
        signEnvelopeSubService = new CommissionSignEnvelopeSubService(envelopeKeyPair, commissionDbOperations);
        createAccountSubService = new CommissionCreateAccountSubService();
    }

    public CompletionStage<CommissionInitResponse> init(CommissionInitRequest request, VerifiedJwt jwt) {
        logger.info("init(): request = {}, userId = {}", request.toString(), jwt.getUserId());
        return initSubService.init(request, jwt);
    }

    public CompletionStage<CommissionSignEnvelopeResponse> signEnvelope(CommissionSignEnvelopeRequest request, VerifiedJwt jwt) {
        logger.info("signEnvelope(): user: {}, voting: {}", jwt.getUserId(), jwt.getVotingId());
        return signEnvelopeSubService.signEnvelope(request, jwt);
    }

    public CompletionStage<CommissionAccountCreationResponse> createAccount(CommissionAccountCreationRequest request) {
        logger.info("createAccount(): request = {}", request);
        return createAccountSubService.createAccount(request);
    }

    // TODO: Request account creation message: votingId|voterPublicAccountId
    //       Store signature in DB. In case the message is replayed, it can be checked whether the account
    //       has already been created or not.
}
