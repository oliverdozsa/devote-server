package security;

import com.auth0.jwt.algorithms.Algorithm;
import com.typesafe.config.Config;

import javax.inject.Inject;
import javax.inject.Provider;

public class UuidAuthAlgorithmProvider implements Provider<Algorithm> {
    private final Algorithm algorithm;

    @Inject
    public UuidAuthAlgorithmProvider(Config config){
        String secret = config.getString("play.http.secret.key");
        algorithm = Algorithm.HMAC256(secret);
    }

    @Override
    public Algorithm get() {
        return algorithm;
    }
}
