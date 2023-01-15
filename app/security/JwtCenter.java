package security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;
import play.Logger;
import play.libs.F;
import security.jwtverification.JwtVerification;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class JwtCenter {
    private final JwtVerification jwtVerification;
    private final Config config;
    private final Algorithm algoForTokenAuth;

    private static final Logger.ALogger logger = Logger.of(JwtCenter.class);

    @Inject
    public JwtCenter(@Named("central") JwtVerification jwtVerification, Config config, @Named("tokenAuth") Algorithm algorithmForTokenAuth) {
        this.jwtVerification = jwtVerification;
        this.config = config;
        this.algoForTokenAuth = algorithmForTokenAuth;
    }

    public F.Either<Error, VerifiedJwt> verify(String token) {
        try {
            DecodedJWT jwt = jwtVerification.verify(token);
            return F.Either.Right(new VerifiedJwt(jwt, config));
        } catch (JWTVerificationException e) {
            logger.warn("Failed to verify token!", e);
            return F.Either.Left(Error.ERR_INVALID_SIGNATURE_OR_CLAIM);
        }
    }

    public String createTokenAuthJwt(String userId) {
        String issuer = config.getString("devote.jwt.token.auth.issuer");
        int expiryMins = config.getInt("devote.jwt.token.auth.token.expiry.mins");
        String rolesClaim = config.getString("devote.jwt.roles.claim");

        String[] roles = new String[]{"voter"};
        if(config.getBoolean("devote.scale.test.mode")) {
            roles = new String[]{"voter", "vote-caller"};
        }

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(userId)
                .withArrayClaim(rolesClaim, roles)
                .withExpiresAt(Date.from(Instant.now().plus(expiryMins, ChronoUnit.MINUTES)))
                .sign(algoForTokenAuth);
    }

    public enum Error {
        ERR_INVALID_SIGNATURE_OR_CLAIM
    }
}
