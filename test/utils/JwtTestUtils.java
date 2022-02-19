package utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.typesafe.config.Config;
import play.libs.Json;
import play.mvc.Http;
import security.UserInfoCollectorForTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

public class JwtTestUtils {
    private final Config config;
    private final Algorithm algorithm;

    public JwtTestUtils(Config config) {
        this.config = config;

        String secret = config.getString("play.http.secret.key");
        algorithm = Algorithm.HMAC256(secret);
    }

    public String createToken(String userId) {
        return createToken(5, userId);
    }

    public String createToken(int expirySeconds, String userId) {
        Date expiresAt = Date.from(Instant.now().plus(expirySeconds, ChronoUnit.SECONDS));
        return createToken(expiresAt, userId, new String[]{"voter", "vote-caller"});
    }

    public String createToken(String userId, String[] roles) {
        Date expiresAt = Date.from(Instant.now().plus(5, ChronoUnit.SECONDS));
        return createToken(expiresAt, userId, roles);
    }

    public String createToken(Date expiresAt, String userId) {
        return createToken(expiresAt, userId, new String[]{"voter", "vote-caller"});
    }

    public String createToken(Date expiresAt, String userId, String[] roles) {
        String rolesClaim = config.getString("devote.jwt.roles.claim");

        return prepareBuilder(expiresAt)
                .withSubject(userId)
                .withArrayClaim(rolesClaim, roles)
                .sign(algorithm);
    }

    public static void addJwtTokenTo(Http.RequestBuilder httpRequest, String token) {
        httpRequest.header("Authorization", "Bearer " + token);
    }

    private JWTCreator.Builder prepareBuilder(Date expiresAt) {
        String issuer = config.getString("devote.jwt.issuer");


        return JWT.create()
                .withIssuer(issuer)
                .withExpiresAt(expiresAt);
    }
}
