package services.voting.commissionsubs;

import data.entities.JpaCommissionSession;
import data.entities.JpaStoredTransaction;
import data.operations.CommissionDbOperations;
import play.Logger;
import responses.voting.CommissionGetEnvelopeSignatureResponse;
import responses.voting.CommissionTransactionOfSignatureResponse;
import services.Base62Conversions;

import java.util.concurrent.CompletionStage;

import static utils.StringUtils.redactWithEllipsis;

public class CommissionStoredDataSubService {
    private final CommissionDbOperations commissionDbOperations;

    private static final Logger.ALogger logger = Logger.of(CommissionStoredDataSubService.class);

    public CommissionStoredDataSubService(CommissionDbOperations commissionDbOperations) {
        this.commissionDbOperations = commissionDbOperations;
    }

    public CompletionStage<CommissionTransactionOfSignatureResponse> transactionOfSignature(String signature) {
        logger.info("transactionOfSignature(): signature = {}", redactWithEllipsis(signature, 5));

        return commissionDbOperations.getTransaction(signature)
                .thenApply(CommissionStoredDataSubService::toResponse);
    }

    public CompletionStage<CommissionGetEnvelopeSignatureResponse> signatureOfEnvelope(String votingId, String user) {
        logger.info("signatureOfEnvelope(): votingId = {}, user = {}", votingId, user);

        Long votingIdAsLong = Base62Conversions.decode(votingId);
        return commissionDbOperations.getCommissionSessionWithExistingEnvelopeSignature(votingIdAsLong, user)
                .thenApply(CommissionStoredDataSubService::toResponse);
    }

    private static CommissionTransactionOfSignatureResponse toResponse(JpaStoredTransaction jpaStoredTransaction) {
        CommissionTransactionOfSignatureResponse response = new CommissionTransactionOfSignatureResponse();
        response.setTransaction(jpaStoredTransaction.getTransaction());
        return response;
    }

    private static CommissionGetEnvelopeSignatureResponse toResponse(JpaCommissionSession jpaCommissionSession) {
        CommissionGetEnvelopeSignatureResponse response = new CommissionGetEnvelopeSignatureResponse();
        response.setEnvelopeSignatureBase64(jpaCommissionSession.getEnvelopeSignature());

        return response;
    }
}
