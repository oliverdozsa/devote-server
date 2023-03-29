package security.jwtverification;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;

import javax.inject.Inject;

public class JwtVerificationForTests implements JwtVerification {
    private final String issuer;
    private final Algorithm algorithm;

    @Inject
    public JwtVerificationForTests(Config config) {
        String secret = config.getString("play.http.secret.key");
        algorithm = Algorithm.HMAC256(secret);

        issuer = config.getString("galactic.vote.jwt.issuer");
    }

    @Override
    public DecodedJWT verify(String token) {
        DecodedJWT jwt = JWT.decode(token);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();

        return verifier.verify(jwt);
    }
}
