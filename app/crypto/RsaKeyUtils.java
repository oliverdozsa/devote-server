package crypto;

import exceptions.CryptoException;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import play.api.Play;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class RsaKeyUtils {
    public static String publicKeyToPemString(AsymmetricCipherKeyPair keyPair) {
        try {
            RSAKeyParameters publicKey = (RSAKeyParameters) keyPair.getPublic();
            byte[] publicKeyBytes =  SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(publicKey).getEncoded();
            return keyToPemString("PUBLIC KEY", publicKeyBytes);
        } catch (IOException e) {
            throw new CryptoException("Failed to create publiy key PEM string", e);
        }
    }

    public static AsymmetricCipherKeyPair readEncryptedKey(String password, String pemFilePath) {
        try {
            PEMEncryptedKeyPair encryptedKeyPair = readEncryptedKeyPair(pemFilePath);
            PEMDecryptorProvider decryptorProvider = new JcePEMDecryptorProviderBuilder()
                    .build(password.toCharArray());
            PEMKeyPair pemKeyPair = encryptedKeyPair.decryptKeyPair(decryptorProvider);

            AsymmetricKeyParameter privateKey = PrivateKeyFactory.createKey(pemKeyPair.getPrivateKeyInfo());
            AsymmetricKeyParameter publicKey = PublicKeyFactory.createKey(pemKeyPair.getPublicKeyInfo());
            return new AsymmetricCipherKeyPair(publicKey, privateKey);
        } catch (IOException e) {
            throw new CryptoException("Failed to read pem file: " + pemFilePath, e);
        }
    }

    private static PEMEncryptedKeyPair readEncryptedKeyPair(String pemFilePath) throws IOException {
        InputStream inputStream = Play.class.getClassLoader().getResourceAsStream(pemFilePath);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        PEMParser pemParser = new PEMParser(inputStreamReader);
        return (PEMEncryptedKeyPair) pemParser.readObject();
    }

    private static String keyToPemString(String keyType, byte[] keyBytes) throws IOException {
        PemObject pemObject = new PemObject(keyType, keyBytes);
        StringWriter keyStringWriter = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(keyStringWriter);
        pemWriter.writeObject(pemObject);
        pemWriter.close();

        return keyStringWriter.toString().replace("\n", "");
    }
}
