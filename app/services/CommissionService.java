package services;

import data.operations.CommissionDbOperations;
import data.operations.VotingDbOperations;
import devote.blockchain.operations.CommissionBlockchainOperations;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import play.Logger;
import requests.CommissionAccountCreationRequest;
import requests.CommissionInitRequest;
import requests.CommissionSignEnvelopeRequest;
import requests.CommissionTransactionOfSignatureRequest;
import responses.CommissionAccountCreationResponse;
import responses.CommissionInitResponse;
import responses.CommissionSignEnvelopeResponse;
import responses.CommissionTransactionOfSignatureResponse;
import security.JwtCenter;
import security.VerifiedJwt;
import services.commissionsubs.CommissionCreateAccountSubService;
import services.commissionsubs.CommissionInitSubService;
import services.commissionsubs.CommissionSignEnvelopeSubService;
import services.commissionsubs.CommissionTxOfSignatureSubService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletionStage;

import static crypto.RsaKeyUtils.publicKeyToPemString;

public class CommissionService {
    private static final Logger.ALogger logger = Logger.of(CommissionService.class);

    private final CommissionInitSubService initSubService;
    private final CommissionSignEnvelopeSubService signEnvelopeSubService;
    private final CommissionCreateAccountSubService createAccountSubService;
    private final CommissionTxOfSignatureSubService txOfSignatureSubService;

    @Inject
    public CommissionService(
            @Named("envelope") AsymmetricCipherKeyPair envelopeKeyPair,
            CommissionDbOperations commissionDbOperations,
            VotingDbOperations votingDbOperations,
            CommissionBlockchainOperations commissionBlockchainOperations,
            JwtCenter jwtCenter
    ) {
        initSubService = new CommissionInitSubService(publicKeyToPemString(envelopeKeyPair), jwtCenter, commissionDbOperations);
        signEnvelopeSubService = new CommissionSignEnvelopeSubService(envelopeKeyPair, commissionDbOperations);
        createAccountSubService = new CommissionCreateAccountSubService(commissionDbOperations, votingDbOperations, commissionBlockchainOperations, envelopeKeyPair);
        txOfSignatureSubService = new CommissionTxOfSignatureSubService(commissionDbOperations);
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

    public CompletionStage<CommissionTransactionOfSignatureResponse> transactionOfSignature(
            CommissionTransactionOfSignatureRequest request
    ) {
        logger.info("transactionOfSignature(): request = {}", request);
        return txOfSignatureSubService.transactionOfSignature(request);
    }
}
