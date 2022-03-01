package crypto;

import services.Base62Conversions;

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

    public static void main(String[] args) {
        String key = generateKey();
        String encryptedCode = encryptOptionCode(key, 42);
        System.out.println(encryptedCode);
        System.out.println(Base62Conversions.encode(4242L));
    }
}
