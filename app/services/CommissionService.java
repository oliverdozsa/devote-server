package services;

import crypto.EncryptedVoting;
import crypto.RsaKeyUtils;
import data.entities.JpaVoting;
import data.operations.CommissionDbOperations;
import data.operations.VoterDbOperations;
import data.operations.VotingDbOperations;
import devote.blockchain.operations.CommissionBlockchainOperations;
import exceptions.BusinessLogicViolationException;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import play.Logger;
import requests.CommissionAccountCreationRequest;
import requests.CommissionInitRequest;
import requests.CommissionSignEnvelopeRequest;
import responses.CommissionAccountCreationResponse;
import responses.CommissionGetAnEncryptedOptionCodeResponse;
import responses.CommissionGetEnvelopeSignatureResponse;
import responses.CommissionInitResponse;
import responses.CommissionSignEnvelopeResponse;
import responses.CommissionTransactionOfSignatureResponse;
import security.JwtCenter;
import security.VerifiedJwt;
import services.commissionsubs.CommissionCreateAccountSubService;
import services.commissionsubs.CommissionInitSubService;
import services.commissionsubs.CommissionSignEnvelopeSubService;
import services.commissionsubs.CommissionStoredDataSubService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletionStage;

import static crypto.RsaKeyUtils.publicKeyToPemString;
import static java.util.concurrent.CompletableFuture.runAsync;
import static utils.StringUtils.redactWithEllipsis;

public class CommissionService {
    private static final Logger.ALogger logger = Logger.of(CommissionService.class);

    private final CommissionInitSubService initSubService;
    private final CommissionSignEnvelopeSubService signEnvelopeSubService;
    private final CommissionCreateAccountSubService createAccountSubService;
    private final CommissionStoredDataSubService storedDataSubService;
    private final VotingDbOperations votingDbOperations;

    @Inject
    public CommissionService(
            @Named("envelope") AsymmetricCipherKeyPair envelopeKeyPair,
            CommissionDbOperations commissionDbOperations,
            VotingDbOperations votingDbOperations,
            CommissionBlockchainOperations commissionBlockchainOperations,
            VoterDbOperations voterDbOperations
    ) {
        initSubService = new CommissionInitSubService(publicKeyToPemString(envelopeKeyPair), commissionDbOperations, voterDbOperations);
        signEnvelopeSubService = new CommissionSignEnvelopeSubService(envelopeKeyPair, commissionDbOperations);
        createAccountSubService = new CommissionCreateAccountSubService(commissionDbOperations, votingDbOperations, commissionBlockchainOperations, envelopeKeyPair);
        storedDataSubService = new CommissionStoredDataSubService(commissionDbOperations);
        this.votingDbOperations = votingDbOperations;
    }

    public CompletionStage<CommissionInitResponse> init(CommissionInitRequest request, VerifiedJwt jwt) {
        logger.info("init(): request = {}, userId = {}", request.toString(), jwt.getUserId());
        return initSubService.init(request, jwt);
    }

    public CompletionStage<CommissionSignEnvelopeResponse> signEnvelope(CommissionSignEnvelopeRequest request, VerifiedJwt jwt, String votingId) {
        logger.info("signEnvelope(): user: {}, voting: {}", jwt.getUserId(), votingId);
        return signEnvelopeSubService.signEnvelope(request, jwt, votingId);
    }

    public CompletionStage<CommissionAccountCreationResponse> createAccount(CommissionAccountCreationRequest request) {
        logger.info("createAccount(): request = {}", request);
        return createAccountSubService.createAccount(request);
    }

    public CompletionStage<CommissionTransactionOfSignatureResponse> transactionOfSignature(String signature) {
        logger.info("transactionOfSignature(): signature = {}", redactWithEllipsis(signature, 5));
        return storedDataSubService.transactionOfSignature(signature);
    }

    public CompletionStage<CommissionGetEnvelopeSignatureResponse> signatureOfEnvelope(String votingId, String user) {
        logger.info("signatureOfEnvelope(): votingId = {}, user = {}", votingId, user);
        return storedDataSubService.signatureOfEnvelope(votingId, user);
    }

    public CompletionStage<CommissionGetAnEncryptedOptionCodeResponse> encryptOptionCode(String votingId, Integer optionCode) {
        logger.info("encryptOptionCode(): votingId = {}", Base62Conversions.decode(votingId));
        return checkIfOptionCodeIsValid(optionCode)
                .thenCompose(v -> Base62Conversions.decodeAsStage(votingId))
                .thenCompose(votingDbOperations::single)
                .thenApply(CommissionService::getEncryptionKeyFrom)
                .thenApply(key -> EncryptedVoting.encryptOptionCode(key, optionCode))
                .thenApply(CommissionService::toResponse);

    }

    private static CompletionStage<Void> checkIfOptionCodeIsValid(Integer optionCode) {
        return runAsync(() -> {
            if (optionCode < 1 || optionCode > 99) {
                String message = "Option code must be > 0 and < 100, but it was " + optionCode;
                logger.warn("checkIfOptionCodeIsValid()" + message);
                throw new BusinessLogicViolationException(message);
            }
        });
    }

    private static String getEncryptionKeyFrom(JpaVoting voting) {
        if (voting.getEncryptionKey() == null || voting.getEncryptionKey().length() == 0) {
            String message = String.format("Encryption of option code has been requested, but voting %d is not encrypted!", voting.getId());
            logger.warn("getEncryptionKeyFrom()" + message);
            throw new BusinessLogicViolationException(message);
        }

        return voting.getEncryptionKey();
    }

    private static CommissionGetAnEncryptedOptionCodeResponse toResponse(String encryptedOptionCode) {
        CommissionGetAnEncryptedOptionCodeResponse response = new CommissionGetAnEncryptedOptionCodeResponse();
        response.setResult(encryptedOptionCode);
        return response;
    }
}
