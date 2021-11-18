package crypto;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class AesCtrCryptoTest {
    @Test
    public void testEncryptDecrypt() {
        // Given
        byte[] randomKey = AesCtrCrypto.generateKey();
        String message = "The quick brown fox jumps over the lazy dog";
        byte[] messageBytes = message.getBytes();

        // When
        byte[] cipher = AesCtrCrypto.encrypt(randomKey, messageBytes);

        // Then
        String decodedMessage = decryptToString(randomKey, cipher);
        assertThat(decodedMessage, equalTo("The quick brown fox jumps over the lazy dog"));
    }

    @Test
    public void testEncryptingSameMessageProducesDifferentCipher() {
        // Given
        byte[] randomKey = AesCtrCrypto.generateKey();
        String message = "The quick brown fox jumps over the lazy dog";
        byte[] messageBytes = message.getBytes();

        // When
        byte[] cipher_a = AesCtrCrypto.encrypt(randomKey, messageBytes);
        byte[] cipher_b = AesCtrCrypto.encrypt(randomKey, messageBytes);

        // Then
        boolean areCiphersDifferent = !Arrays.equals(cipher_a, cipher_b);
        assertTrue("Ciphers should have been different!", areCiphersDifferent);

        String decodedMessage = decryptToString(randomKey, cipher_a);
        assertThat(decodedMessage, equalTo("The quick brown fox jumps over the lazy dog"));

        decodedMessage = decryptToString(randomKey, cipher_b);
        assertThat(decodedMessage, equalTo("The quick brown fox jumps over the lazy dog"));
    }

    private static String decryptToString(byte[] key, byte[] cipher) {
        byte[] decodedMessageBytes = AesCtrCrypto.decrypt(key, cipher);
        return new String(decodedMessageBytes);
    }
}
