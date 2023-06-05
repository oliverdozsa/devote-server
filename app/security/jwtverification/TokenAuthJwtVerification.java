package security.jwtverification;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;

import javax.inject.Inject;
import javax.inject.Named;

public class TokenAuthJwtVerification implements JwtVerification {
    private final String issuer;
    private final JWTVerifier jwtVerifier;

    @Inject
    public TokenAuthJwtVerification(@Named("tokenAuth") Algorithm algorithm, Config config) {
        this.issuer = config.getString("galactic.host.vote.jwt.token.auth.issuer");
        jwtVerifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
    }

    @Override
    public DecodedJWT verify(String token) {
        try {
            return jwtVerifier.verify(token);
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify with token auth", e);
        }
    }
}
