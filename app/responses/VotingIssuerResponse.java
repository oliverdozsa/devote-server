package responses;

public class VotingIssuerResponse {
    private String issuerAccountId;
    private String assetCode;

    public String getIssuerAccountId() {
        return issuerAccountId;
    }

    public void setIssuerAccountId(String issuerAccountId) {
        this.issuerAccountId = issuerAccountId;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }
}
