package responses;

public class CommissionInitResponse {
    private String publicKey;

    private String sessionJwt;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getSessionJwt() {
        return sessionJwt;
    }

    public void setSessionJwt(String sessionJwt) {
        this.sessionJwt = sessionJwt;
    }

    @Override
    public String toString() {
        return "CommissionInitResponse{" +
                "publicKey='" + publicKey + '\'' +
                ", sessionJwt='" + sessionJwt + '\'' +
                '}';
    }
}
