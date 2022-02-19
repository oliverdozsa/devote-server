package utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.typesafe.config.Config;
import play.mvc.Http;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

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
        return createToken(expiresAt, userId);
    }

    public String createToken(Date expiresAt, String userId) {
        return prepareBuilder(expiresAt)
                .withSubject(userId)
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
