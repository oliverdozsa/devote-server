package crypto;

import java.util.Base64;

public class EncryptedVoting {
    public static String generateKey() {
        byte[] keyBytes = AesCtrCrypto.generateKey();
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    private EncryptedVoting() {
    }
}
