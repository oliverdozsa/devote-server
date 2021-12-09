package responses;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSABlindingFactorGenerator;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSABlindingParameters;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;

import java.math.BigInteger;
import java.security.SecureRandom;

public class CastVoteInitResponse {
    // TODO

    public static void main(String[] args) {
        byte[] message = new byte[]{41, 21, 84, 10};
        AsymmetricCipherKeyPair keyPair = generateKeyPair();
        RSAKeyParameters publicKey = (RSAKeyParameters) keyPair.getPublic();

        RSABlindingFactorGenerator blindingFactorGenerator = new RSABlindingFactorGenerator();
        blindingFactorGenerator.init(publicKey);
        BigInteger blindingFactor = blindingFactorGenerator.generateBlindingFactor();
        // TODO: This is the public session params (nonce, public key -> blindingFactor, public key)
        RSABlindingParameters blindingParameters = new RSABlindingParameters(publicKey, blindingFactor);

        // TODO: User (client) then creates a concealed message using blindingParameters (RsaProtoVoteImp)
        // TODO: Server signs the concealed message (RsaCommissionImp.sign()), and sends back the signature
        // TODO: User then comes back anonymously with signature and revealed message and requests an account to be
       //        created (==gives an account id where one vote token should be paid)

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
}
