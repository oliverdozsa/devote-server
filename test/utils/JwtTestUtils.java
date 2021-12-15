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
        String userIdClaim = config.getString("devote.jwt.useridclaim");

        return prepareBuilder(expirySeconds)
                .withClaim(userIdClaim, userId)
                .sign(algorithm);
    }

    public static void addJwtTokenTo(Http.RequestBuilder httpRequest, String token) {
        httpRequest.header("Authorization", "Bearer " + token);
    }

    private JWTCreator.Builder prepareBuilder(int expirySeconds) {
        String issuer = config.getString("devote.jwt.issuer");
        return JWT.create()
                .withIssuer(issuer)
                .withExpiresAt(Date.from(Instant.now().plus(expirySeconds, ChronoUnit.SECONDS)));
    }
}
