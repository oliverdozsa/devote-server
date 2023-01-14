package security.jwtverification;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Named;

public class JwtCentralVerification implements JwtVerification {
    private final JwtVerification auth0JwtVerification;
    private final JwtVerification tokenAuthJwtVerification;
    private final String tokenAuthSubjectPrefix;

    private static final Logger.ALogger logger = Logger.of(JwtCentralVerification.class);

    @Inject
    public JwtCentralVerification(@Named("auth0") JwtVerification auth0JwtVerification,
                                  @Named("tokenAuth") JwtVerification tokenAuthJwtVerification,
                                  Config config) {
        this.auth0JwtVerification = auth0JwtVerification;
        this.tokenAuthJwtVerification = tokenAuthJwtVerification;
        tokenAuthSubjectPrefix = config.getString("devote.jwt.token.auth.subject.prefix");
    }

    @Override
    public DecodedJWT verify(String token) {
        DecodedJWT unverifiedDecodedJWT = JWT.decode(token);
        String subject = unverifiedDecodedJWT.getSubject();
        if (subject != null && subject.startsWith(tokenAuthSubjectPrefix)) {
            logger.info("Verifying with token auth verifier.");
            return tokenAuthJwtVerification.verify(token);
        } else {
            logger.info("Verifying with auth0 verifier.");
            return auth0JwtVerification.verify(token);
        }
    }
}
