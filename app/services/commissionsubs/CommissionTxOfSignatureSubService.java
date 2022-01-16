package services.commissionsubs;

import data.entities.JpaStoredTransaction;
import data.operations.CommissionDbOperations;
import play.Logger;
import responses.CommissionTransactionOfSignatureResponse;

import java.util.concurrent.CompletionStage;

import static utils.StringUtils.redactWithEllipsis;

public class CommissionTxOfSignatureSubService {
    private final CommissionDbOperations commissionDbOperations;

    private static final Logger.ALogger logger = Logger.of(CommissionTxOfSignatureSubService.class);

    public CommissionTxOfSignatureSubService(CommissionDbOperations commissionDbOperations) {
        this.commissionDbOperations = commissionDbOperations;
    }

    public CompletionStage<CommissionTransactionOfSignatureResponse> transactionOfSignature(String signature) {
        logger.info("transactionOfSignature(): signature = {}", redactWithEllipsis(signature, 5));

        return commissionDbOperations.getTransaction(signature)
                .thenApply(CommissionTxOfSignatureSubService::toResponse);
    }

    private static CommissionTransactionOfSignatureResponse toResponse(JpaStoredTransaction jpaStoredTransaction) {
        CommissionTransactionOfSignatureResponse response = new CommissionTransactionOfSignatureResponse();
        response.setTransaction(jpaStoredTransaction.getTransaction());
        return response;
    }
}
