package requests;

import play.data.validation.Constraints;

import static utils.StringUtils.redact;
import static utils.StringUtils.redactWithEllipsis;

public class CommissionAccountCreationRequest {
    @Constraints.Required
    @Constraints.Pattern(".+|.+")
    private String message;

    @Constraints.Required
    private String revealedSignatureBase64;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRevealedSignatureBase64() {
        return revealedSignatureBase64;
    }

    public void setRevealedSignatureBase64(String revealedSignatureBase64) {
        this.revealedSignatureBase64 = revealedSignatureBase64;
    }

    @Override
    public String toString() {
        return "CommissionAccountCreationRequest{" +
                "message='" + redactWithEllipsis(message, 10) +
                ", revealedSignatureBase64 ='" + redactWithEllipsis(revealedSignatureBase64, 5) +
                '}';
    }
}
