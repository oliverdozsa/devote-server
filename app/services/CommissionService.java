package services;

import crypto.EncryptedVoting;
import data.entities.JpaVoting;
import data.operations.CommissionDbOperations;
import data.operations.VoterDbOperations;
import data.operations.VotingDbOperations;
import devote.blockchain.operations.CommissionBlockchainOperations;
import exceptions.BusinessLogicViolationException;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import play.Logger;
import requests.CommissionCreateTransactionRequest;
import requests.CommissionInitRequest;
import requests.CommissionSignEnvelopeRequest;
import responses.CommissionAccountCreationResponse;
import responses.CommissionGetAnEncryptedChoiceResponse;
import responses.CommissionGetEnvelopeSignatureResponse;
import responses.CommissionInitResponse;
import responses.CommissionSignEnvelopeResponse;
import responses.CommissionTransactionOfSignatureResponse;
import security.VerifiedJwt;
import services.commissionsubs.CommissionCreateTransactionSubService;
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
    private final CommissionCreateTransactionSubService createTransactionSubService;
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
        createTransactionSubService = new CommissionCreateTransactionSubService(commissionDbOperations, votingDbOperations, commissionBlockchainOperations, envelopeKeyPair);
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

    public CompletionStage<CommissionAccountCreationResponse> createTransaction(CommissionCreateTransactionRequest request) {
        logger.info("createTransaction(): request = {}", request);
        return createTransactionSubService.createTransaction(request);
    }

    public CompletionStage<CommissionTransactionOfSignatureResponse> transactionOfSignature(String signature) {
        logger.info("transactionOfSignature(): signature = {}", redactWithEllipsis(signature, 5));
        return storedDataSubService.transactionOfSignature(signature);
    }

    public CompletionStage<CommissionGetEnvelopeSignatureResponse> signatureOfEnvelope(String votingId, String user) {
        logger.info("signatureOfEnvelope(): votingId = {}, user = {}", votingId, user);
        return storedDataSubService.signatureOfEnvelope(votingId, user);
    }

    public CompletionStage<CommissionGetAnEncryptedChoiceResponse> encryptChoice(String votingId, String choice) {
        logger.info("encryptOptionCode(): votingId = {}", Base62Conversions.decode(votingId));
        return checkIfChoiceIsValid(choice)
                .thenCompose(v -> Base62Conversions.decodeAsStage(votingId))
                .thenCompose(votingDbOperations::single)
                .thenApply(CommissionService::getEncryptionKeyFrom)
                .thenApply(key -> EncryptedVoting.encryptChoice(key, choice))
                .thenApply(CommissionService::toResponse);

    }

    private static CompletionStage<Void> checkIfChoiceIsValid(String choice) {
        return runAsync(() -> {
            if(choice == null || !choice.matches("^[0-9]{4}$")) {
                throw new BusinessLogicViolationException("Choice must not be empty, and must consist of 4 numeric characters");
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

    private static CommissionGetAnEncryptedChoiceResponse toResponse(String encryptedOptionCode) {
        CommissionGetAnEncryptedChoiceResponse response = new CommissionGetAnEncryptedChoiceResponse();
        response.setResult(encryptedOptionCode);
        return response;
    }
}
