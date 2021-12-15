package security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;
import play.Logger;
import play.libs.F;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class JwtCenter {
    private final Config config;
    private final Algorithm algorithm;
    private final JWTVerifier jwtVerifier;

    private static final Logger.ALogger logger = Logger.of(JwtCenter.class);

    @Inject
    public JwtCenter(Config config) {
        this.config = config;

        String secret = config.getString("play.http.secret.key");
        algorithm = Algorithm.HMAC256(secret);

        String issuer = config.getString("devote.jwt.issuer");
        jwtVerifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
    }

    public F.Either<Error, VerifiedJwt> verify(String token) {
        try{
            DecodedJWT jwt = jwtVerifier.verify(token);
            return F.Either.Right(new VerifiedJwt(jwt, config));
        } catch (JWTVerificationException e) {
            logger.warn("Failed to verify token!", e);
            return F.Either.Left(Error.ERR_INVALID_SIGNATURE_OR_CLAIM);
        }
    }

    public String create(Long votingId, String userId) {
        String issuer = config.getString("devote.jwt.issuer");
        String userIdClaim = config.getString("devote.jwt.useridclaim");
        String votingIdClaim = config.getString("devote.jwt.votingidclaim");
        int expiryMins = config.getInt("devote.jwt.expiry.mins");

        return JWT.create()
                .withIssuer(issuer)
                .withClaim(userIdClaim, userId)
                .withClaim(votingIdClaim, votingId)
                .withExpiresAt(Date.from(Instant.now().plus(expiryMins, ChronoUnit.MINUTES)))
                .sign(algorithm);
    }

    public enum Error {
        ERR_INVALID_SIGNATURE_OR_CLAIM
    }
}
