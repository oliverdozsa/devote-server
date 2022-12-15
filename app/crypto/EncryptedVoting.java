package crypto;

import java.util.Base64;

public class EncryptedVoting {
    public static String generateKey() {
        byte[] keyBytes = AesCtrCrypto.generateKey();
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    public static String encryptChoice(String encryptionKey, String choice) {
        byte[] keyBytes = Base64.getDecoder().decode(encryptionKey);
        byte[] encryptedBytes = AesCtrCrypto.encrypt(keyBytes, choice.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private EncryptedVoting() {
    }
}
