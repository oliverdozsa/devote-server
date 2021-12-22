package requests;

import play.data.validation.Constraints;

public class CommissionSignEnvelopeRequest {
    @Constraints.Required
    private String envelopeBase64;

    public String getEnvelopeBase64() {
        return envelopeBase64;
    }

    public void setEnvelopeBase64(String envelopeBase64) {
        this.envelopeBase64 = envelopeBase64;
    }

    @Override
    public String toString() {
        return "CommissionSignEnvelopeRequest{" +
                "envelopeBase64='" + envelopeBase64 + '\'' +
                '}';
    }
}
