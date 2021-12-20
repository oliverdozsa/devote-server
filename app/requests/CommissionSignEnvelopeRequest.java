package requests;

public class CommissionSignEnvelopeRequest {
    private String envelope;

    public String getEnvelope() {
        return envelope;
    }

    public void setEnvelope(String envelope) {
        this.envelope = envelope;
    }

    @Override
    public String toString() {
        return "CommissionSignEnvelopeRequest{" +
                "envelope='" + envelope + '\'' +
                '}';
    }
}
