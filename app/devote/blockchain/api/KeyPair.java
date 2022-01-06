package devote.blockchain.api;

import java.util.Objects;

public class KeyPair {
    public final String secretKey;
    public final String publicKey;

    public KeyPair(String secretKey, String publicKey) {
        this.secretKey = secretKey;
        this.publicKey = publicKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyPair keyPair = (KeyPair) o;
        return secretKey.equals(keyPair.secretKey) && publicKey.equals(keyPair.publicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(secretKey, publicKey);
    }
}
