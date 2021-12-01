package crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

// https://www.baeldung.com/java-rsa
public class RsaCrypto {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final int GENERATED_KEY_LENGTH = 2048;

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(GENERATED_KEY_LENGTH);
            KeyPair keyPair = generator.generateKeyPair();
            return keyPair;
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Failed to generate key for RSA!", e);
        }
    }

    public static void main(String[] args) {
        KeyPair keyPair = generateKeyPair();

        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        System.out.println("Private key:" + Base64.getEncoder().encodeToString(privateKeyBytes));

        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        System.out.println("Public key:" + Base64.getEncoder().encodeToString(publicKeyBytes));

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            privateKeyBytes = privateKey.getEncoded();
            System.out.println("Private key:" + Base64.getEncoder().encodeToString(privateKeyBytes));

            publicKeyBytes = publicKey.getEncoded();
            System.out.println("Public key:" + Base64.getEncoder().encodeToString(publicKeyBytes));

            // Secret
            String message = "Hello World!";
            byte[] messageBytes = message.getBytes();

            Cipher encryptCipher = Cipher.getInstance("RSA/None/OAEPWITHSHA-256ANDMGF1PADDING");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encryptedMessageBytes = encryptCipher.doFinal(messageBytes);
            System.out.println("Message bytes 1:" + Base64.getEncoder().encodeToString(encryptedMessageBytes));
            System.out.println("Message bytes 1 size:" + encryptedMessageBytes.length);

            encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            encryptedMessageBytes = encryptCipher.doFinal(messageBytes);
            System.out.println("Message bytes 2:" + Base64.getEncoder().encodeToString(encryptedMessageBytes));
            System.out.println("Message bytes 2 size:" + encryptedMessageBytes.length);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
