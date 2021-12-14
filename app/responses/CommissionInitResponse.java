package responses;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CommissionInitResponse {
    private String publicKey;

    @JsonIgnore
    private String sessionToken;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    @Override
    public String toString() {
        return "CastVoteInitResponse{" +
                "publicKey='" + publicKey + '\'' +
                ", sessionToken='" + sessionToken + '\'' +
                '}';
    }
}
