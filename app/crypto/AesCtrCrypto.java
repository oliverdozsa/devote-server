package crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;

// Based on:
//   - https://medium.com/lumenauts/sending-secret-and-anonymous-memos-with-stellar-8914479e949b
//   - https://github.com/travisdazell/AES-CTR-BOUNCYCASTLE/blob/master/AES%20CTR%20Example/src/net/travisdazell/crypto/aes/example/AesCtrExample.scala
public class AesCtrCrypto {
    public static final int RANDOM_IV_LENGTH = 8;

    private static final char[] hexSymbols = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static final SecureRandom secureRandom = new SecureRandom();

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static byte[] encrypt(byte[] key, byte[] message) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        // TODO: Move to separate function
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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        // TODO
        return null;
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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        // TODO
        return null;
    }

    private static byte[] hexStringToByteArray(String s) {
        int length = s.length();
        byte[] result = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            int topDigit = Character.digit(s.charAt(i), 16);
            int bottomDigit = Character.digit(s.charAt(i + 1), 16);
            int combined = (topDigit << 4) + bottomDigit;
            result[i / 2] = (byte) combined;
        }

        return result;
    }

    private static byte[] randomIv() {
        byte[] result = new byte[RANDOM_IV_LENGTH];
        secureRandom.nextBytes(result);
        return result;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            int byteAsInt = bytes[i] & 0xFF;
            hexChars[i * 2] = hexSymbols[byteAsInt >>> 4];
            hexChars[i * 2 + 1] = hexSymbols[byteAsInt & 0x0F];
        }

        return new String(hexChars);
    }

    public static void main(String[] args) {
        byte[] key = Base64.getDecoder().decode("12345678123456781234567812345678");
        byte[] message = "Hello World".getBytes();

        byte[] encrypted;
        byte[] decrypted;

        encrypted = encrypt(key, message);
        decrypted = decrypt(key, encrypted);
        System.out.println(Base64.getEncoder().encodeToString(encrypted));
        System.out.println(new String(decrypted));


        encrypted = encrypt(key, message);
        System.out.println(Base64.getEncoder().encodeToString(encrypted));
        System.out.println(new String(decrypted));

        encrypted = encrypt(key, message);
        System.out.println(Base64.getEncoder().encodeToString(encrypted));
        System.out.println(new String(decrypted));

//        System.out.println(encrypt("12345678123456781234567812345678", "1"));
//        System.out.println(encrypt("12345678123456781234567812345678", "1"));
//        System.out.println(encrypt("12345678123456781234567812345678", "1"));
//        System.out.println(encrypt("12345678123456781234567812345678", "1"));
//
//        String secretMessageHex = encrypt("42844221428442214284422142844221", "Hello World!@#()");
//        System.out.println("Secret hex message: " + secretMessageHex);
//        String decryptedMessage = decrypt("42844221428442214284422142844221", secretMessageHex);
//        System.out.println("decryptedMessage: " + decryptedMessage);
    }
}
