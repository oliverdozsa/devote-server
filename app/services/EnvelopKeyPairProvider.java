package services;

import com.typesafe.config.Config;
import crypto.RsaKeyUtils;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import javax.inject.Inject;
import javax.inject.Provider;

public class EnvelopKeyPairProvider implements Provider<AsymmetricCipherKeyPair> {
    private final String password;
    private final String path;

    @Inject
    public EnvelopKeyPairProvider(Config config) {
        password = config.getString("play.http.secret.key");
        // Used only for blind signature! https://en.wikipedia.org/wiki/Blind_signature#Dangers_of_RSA_blind_signing
        path = config.getString("galactic.host.vote.commission.envelope.rsa.key.file");
    }

    @Override
    public AsymmetricCipherKeyPair get() {
        return RsaKeyUtils.readEncryptedKey(password, path);
    }
}
