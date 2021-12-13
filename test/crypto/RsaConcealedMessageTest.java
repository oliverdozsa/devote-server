package crypto;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

public class RsaConcealedMessageTest {
    public static void main(String[] args) {
        AsymmetricCipherKeyPair rsaKeyPair = generateKeyPair();
        Scanner userInput = new Scanner(System.in);

        try {
            printKeyPairPems(rsaKeyPair);

            // Producing signature on concealed message
            System.out.print("Concealed message (in base64)?");
            String concealedMessageBase64 = userInput.nextLine();
            byte[] concealedMessageBytes = Base64.getDecoder().decode(concealedMessageBase64);
            byte[] signatureOnConcealedMessage = signConcealedMessage(concealedMessageBytes, rsaKeyPair.getPrivate());
            System.out.println("Signature on concealed message (base64): " + Base64.getEncoder().encodeToString(signatureOnConcealedMessage));


            // Verifying revealed signature on revealed message
            System.out.print("Revealed message (in base64)?");
            String revealedMessageBase64 = userInput.nextLine();
            System.out.print("Revealed signature (in base64)?");
            String revealedSignatureBase64 = userInput.nextLine();

            byte[] revealedMessageBytes = Base64.getDecoder().decode(revealedMessageBase64);
            System.out.println("Revealed message is: " + new String(revealedMessageBytes));
            byte[] revealedSignatureBytes = Base64.getDecoder().decode(revealedSignatureBase64);

            RSAEngine rsaEngine = new RSAEngine();
            rsaEngine.init(false, rsaKeyPair.getPublic());
            byte[] signatureDecrypted = rsaEngine.processBlock(revealedSignatureBytes, 0, revealedSignatureBytes.length);
            byte[] messageHashed = MessageDigest.getInstance("SHA-256").digest(revealedMessageBytes);
            System.out.println("Revealed signature is verified on revealed message: " + Arrays.equals(messageHashed, signatureDecrypted));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static AsymmetricCipherKeyPair generateKeyPair() {
        RSAKeyPairGenerator generator = new RSAKeyPairGenerator();

        BigInteger publicExponent = new BigInteger("10001", 16);
        SecureRandom random = new SecureRandom();
        RSAKeyGenerationParameters keyGenParams = new RSAKeyGenerationParameters(
                publicExponent, random, 4096, 80
        );

        generator.init(keyGenParams);
        return generator.generateKeyPair();
    }

    private static void printKeyPairPems(AsymmetricCipherKeyPair keyPair) throws IOException {
        RSAKeyParameters publicKey = (RSAKeyParameters) keyPair.getPublic();
        byte[] publicKeyBytes = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(publicKey).getEncoded();
        printKeyPem("PUBLIC KEY", publicKeyBytes);

        RSAKeyParameters privateKey = (RSAKeyParameters) keyPair.getPrivate();
        byte[] privateKeyBytes = PrivateKeyInfoFactory.createPrivateKeyInfo(privateKey).getEncoded();
        printKeyPem("PRIVATE KEY", privateKeyBytes);
    }

    private static void printKeyPem(String keyType, byte[] keyBytes) throws IOException {
        PemObject pemObject = new PemObject(keyType, keyBytes);
        StringWriter keyStringWriter = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(keyStringWriter);
        pemWriter.writeObject(pemObject);
        pemWriter.close();

        System.out.println(keyType + ": " + keyStringWriter.toString().replace("\n", ""));
    }

    private static byte[] signConcealedMessage(byte[] concealedMessage, AsymmetricKeyParameter privateKey) {
        RSAEngine engine = new RSAEngine();
        engine.init(true, privateKey);

        return engine.processBlock(concealedMessage, 0, concealedMessage.length);
    }
}
