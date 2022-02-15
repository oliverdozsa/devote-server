package crypto;

import java.util.Base64;

public class EncryptedVoting {
    public static String generateKey() {
        byte[] keyBytes = AesCtrCrypto.generateKey();
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    public static String encryptOptionCode(String encryptionKey, Integer optionCode) {
        byte[] keyBytes = Base64.getDecoder().decode(encryptionKey);
        String message = Integer.toString(optionCode);
        byte[] encryptedBytes = AesCtrCrypto.encrypt(keyBytes, message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private EncryptedVoting() {
    }
}
