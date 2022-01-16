package services.commissionsubs;

import data.entities.JpaStoredTransaction;
import data.operations.CommissionDbOperations;
import play.Logger;
import requests.CommissionTransactionOfSignatureRequest;
import responses.CommissionTransactionOfSignatureResponse;

import java.util.concurrent.CompletionStage;

public class CommissionTxOfSignatureSubService {
    private final CommissionDbOperations commissionDbOperations;

    private static final Logger.ALogger logger = Logger.of(CommissionTxOfSignatureSubService.class);

    public CommissionTxOfSignatureSubService(CommissionDbOperations commissionDbOperations) {
        this.commissionDbOperations = commissionDbOperations;
    }

    public CompletionStage<CommissionTransactionOfSignatureResponse> transactionOfSignature(
            CommissionTransactionOfSignatureRequest request
    ) {
        logger.info("transactionOfSignature(): request = {}", request);

        return commissionDbOperations.getTransaction(request.getSignature())
                .thenApply(CommissionTxOfSignatureSubService::toResponse);
    }

    private static CommissionTransactionOfSignatureResponse toResponse(JpaStoredTransaction jpaStoredTransaction) {
        CommissionTransactionOfSignatureResponse response = new CommissionTransactionOfSignatureResponse();
        response.setTransaction(jpaStoredTransaction.getTransaction());
        return response;
    }
}
