package requests;

import play.data.validation.Constraints;

import static utils.StringUtils.redactWithEllipsis;

public class CommissionTransactionOfSignatureRequest {
    @Constraints.Required
    private String signature;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "CommissionTransactionOfSignatureRequest{" +
                "signature='" + redactWithEllipsis(signature, 5) + '\'' +
                '}';
    }
}
