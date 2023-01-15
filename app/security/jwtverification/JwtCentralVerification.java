package security.jwtverification;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import play.Logger;
import security.TokenAuthUserIdUtil;

import javax.inject.Inject;
import javax.inject.Named;

public class JwtCentralVerification implements JwtVerification {
    private final JwtVerification auth0JwtVerification;
    private final JwtVerification tokenAuthJwtVerification;
    private final TokenAuthUserIdUtil tokenAuthUserIdUtil;

    private static final Logger.ALogger logger = Logger.of(JwtCentralVerification.class);

    @Inject
    public JwtCentralVerification(@Named("auth0") JwtVerification auth0JwtVerification,
                                  @Named("tokenAuth") JwtVerification tokenAuthJwtVerification,
                                  TokenAuthUserIdUtil tokenAuthUserIdUtil) {
        this.auth0JwtVerification = auth0JwtVerification;
        this.tokenAuthJwtVerification = tokenAuthJwtVerification;
        this.tokenAuthUserIdUtil = tokenAuthUserIdUtil;
    }

    @Override
    public DecodedJWT verify(String token) {
        DecodedJWT unverifiedDecodedJWT = JWT.decode(token);
        String subject = unverifiedDecodedJWT.getSubject();
        if (tokenAuthUserIdUtil.isForTokenAuth(subject)) {
            logger.info("Verifying with token auth verifier.");
            return tokenAuthJwtVerification.verify(token);
        } else {
            logger.info("Verifying with auth0 verifier.");
            return auth0JwtVerification.verify(token);
        }
    }
}
