package security.jwtverification;

import com.auth0.jwt.interfaces.DecodedJWT;

public interface JwtVerification {
    DecodedJWT verify(String token);
}
