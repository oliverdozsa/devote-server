package crypto;

import exceptions.CryptoException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

// Based on:
//   - https://medium.com/lumenauts/sending-secret-and-anonymous-memos-with-stellar-8914479e949b
//   - https://github.com/travisdazell/AES-CTR-BOUNCYCASTLE/blob/master/AES%20CTR%20Example/src/net/travisdazell/crypto/aes/example/AesCtrExample.scala
public class AesCtrCrypto {
    public static final int RANDOM_IV_LENGTH = 8;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int GENERATED_KEY_LENGTH = 256;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static byte[] encrypt(byte[] key, byte[] message) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        try {
            Cipher aes = Cipher.getInstance("AES/CTR/NoPadding", BouncyCastleProvider.PROVIDER_NAME);

            byte[] randomIvBytes = randomIv();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(randomIvBytes);

            aes.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] encryptedBytes = aes.doFinal(message);

            byte[] resultBytes = new byte[randomIvBytes.length + encryptedBytes.length];
            System.arraycopy(randomIvBytes, 0, resultBytes, 0, randomIvBytes.length);
            System.arraycopy(encryptedBytes, 0, resultBytes, randomIvBytes.length, encryptedBytes.length);

            return resultBytes;
        } catch (Exception e) {
            throw new CryptoException("Failed to encrypt with AES CTR!", e);
        }
    }

    public static byte[] decrypt(byte[] key, byte[] cipher) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        try {
            Cipher aes = Cipher.getInstance("AES/CTR/NoPadding", BouncyCastleProvider.PROVIDER_NAME);

            byte[] randomIvBytes = Arrays.copyOfRange(cipher, 0, RANDOM_IV_LENGTH);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(randomIvBytes);

            aes.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] encryptedMessage = Arrays.copyOfRange(cipher, RANDOM_IV_LENGTH, cipher.length);

            return aes.doFinal(encryptedMessage);
        } catch (Exception e) {
            throw new CryptoException("Failed to decrypt with AES CTR!", e);
        }
    }

    public static byte[] generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(GENERATED_KEY_LENGTH);
            return keyGenerator.generateKey().getEncoded();
        } catch (Exception e) {
            throw new CryptoException("Failed to create key for AES CTR!", e);
        }
    }

    private static byte[] randomIv() {
        byte[] result = new byte[RANDOM_IV_LENGTH];
        secureRandom.nextBytes(result);
        return result;
    }

    private AesCtrCrypto() {
    }
}
