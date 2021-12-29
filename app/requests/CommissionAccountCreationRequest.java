package requests;

public class CommissionAccountCreationRequest {
    private String message;
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
}
