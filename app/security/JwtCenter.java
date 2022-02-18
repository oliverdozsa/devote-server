package security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;
import play.Logger;
import play.libs.F;
import security.jwtverification.JwtVerification;

import javax.inject.Inject;

public class JwtCenter {
    private final Config config;
    private final JwtVerification jwtVerification;

    private static final Logger.ALogger logger = Logger.of(JwtCenter.class);

    @Inject
    public JwtCenter(Config config, JwtVerification jwtVerification) {
        this.config = config;
        this.jwtVerification = jwtVerification;
    }

    public F.Either<Error, VerifiedJwt> verify(String token) {
        try{
            DecodedJWT jwt = jwtVerification.verify(token);
            return F.Either.Right(new VerifiedJwt(jwt, config));
        } catch (JWTVerificationException e) {
            logger.warn("Failed to verify token!", e);
            return F.Either.Left(Error.ERR_INVALID_SIGNATURE_OR_CLAIM);
        }
    }

    public enum Error {
        ERR_INVALID_SIGNATURE_OR_CLAIM
    }
}
