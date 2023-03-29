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

    public String createToken(String userId, String email) {
        return createToken(5, userId, email);
    }

    public String createToken(int expirySeconds, String userId, String email) {
        Date expiresAt = Date.from(Instant.now().plus(expirySeconds, ChronoUnit.SECONDS));
        return createToken(expiresAt, userId, new String[]{"voter", "vote-caller"}, email);
    }

    public String createToken(String userId, String[] roles, String email) {
        Date expiresAt = Date.from(Instant.now().plus(5, ChronoUnit.SECONDS));
        return createToken(expiresAt, userId, roles, email);
    }

    public String createToken(Date expiresAt, String userId, String email) {
        return createToken(expiresAt, userId, new String[]{"voter", "vote-caller"}, email);
    }

    public String createToken(Date expiresAt, String userId, String[] roles, String email) {
        String rolesClaim = config.getString("galactic.vote.jwt.roles.claim");
        String emailClaim = config.getString("galactic.vote.jwt.email.claim");
        String emailVerifiedClaim = config.getString("galactic.vote.jwt.email.verified.claim");

        return prepareBuilder(expiresAt)
                .withSubject(userId)
                .withArrayClaim(rolesClaim, roles)
                .withClaim(emailClaim, email)
                .withClaim(emailVerifiedClaim, true)
                .sign(algorithm);
    }

    public static void addJwtTokenTo(Http.RequestBuilder httpRequest, String token) {
        httpRequest.header("Authorization", "Bearer " + token);
    }

    private JWTCreator.Builder prepareBuilder(Date expiresAt) {
        String issuer = config.getString("galactic.vote.jwt.issuer");


        return JWT.create()
                .withIssuer(issuer)
                .withExpiresAt(expiresAt);
    }
}
