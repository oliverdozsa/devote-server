package security.jwtverification;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;

import javax.inject.Inject;
import java.security.interfaces.RSAPublicKey;

public class Auth0JwtVerification implements JwtVerification {
    private final JwkProvider jwkProvider;
    private final String issuer;

    @Inject
    public Auth0JwtVerification(Config config, JwkProvider jwkProvider) {
        this.jwkProvider = jwkProvider;
        issuer = config.getString("galactic.vote.jwt.issuer");
    }

    @Override
    public DecodedJWT verify(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            Jwk jwk = jwkProvider.get(jwt.getKeyId());
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);

            JWTVerifier jwtVerifier = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build();

            jwtVerifier.verify(jwt);

            return jwt;
        } catch (JwkException e) {
            throw new RuntimeException("Failed to verify jwt with auth0", e);
        }
    }
}
