package security.jwtverification;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.checkerframework.checker.units.qual.C;

import java.util.*;

public class JwtVerificationForScaleTesting implements JwtVerification {
    @Override
    public DecodedJWT verify(String token) {
        // Token is not really a jwt; it should merely be a merely an email string
        return new MockDecodedJwt(token);
    }

    private static class MockDecodedJwt implements DecodedJWT {
        private String email;

        public MockDecodedJwt(String email) {
            this.email = email;
        }

        @Override
        public String getToken() {
            return null;
        }

        @Override
        public String getHeader() {
            return null;
        }

        @Override
        public String getPayload() {
            return null;
        }

        @Override
        public String getSignature() {
            return null;
        }

        @Override
        public String getAlgorithm() {
            return null;
        }

        @Override
        public String getType() {
            return null;
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public String getKeyId() {
            return null;
        }

        @Override
        public Claim getHeaderClaim(String name) {
            return null;
        }

        @Override
        public String getIssuer() {
            return null;
        }

        @Override
        public String getSubject() {
            return email;
        }

        @Override
        public List<String> getAudience() {
            return null;
        }

        @Override
        public Date getExpiresAt() {
            return null;
        }

        @Override
        public Date getNotBefore() {
            return null;
        }

        @Override
        public Date getIssuedAt() {
            return null;
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public Claim getClaim(String name) {
            if (name.equals("https://devote.network/roles")) {
                return new EmptyClaim() {
                    @Override
                    public <T> List<T> asList(Class<T> tClazz) throws JWTDecodeException {
                        return (List) (Arrays.asList("voter", "vote-caller"));
                    }
                };
            }

            if (name.equals("https://devote.network/email")) {
                return new EmptyClaim() {
                    @Override
                    public String asString() {
                        return email;
                    }
                };
            }

            if (name.equals("https://devote.network/email-verified")) {
                return new EmptyClaim() {
                    @Override
                    public Boolean asBoolean() {
                        return true;
                    }
                };
            }

            return new EmptyClaim();
        }

        @Override
        public Map<String, Claim> getClaims() {
            return null;
        }
    }

    private static class EmptyClaim implements Claim {

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public Boolean asBoolean() {
            return null;
        }

        @Override
        public Integer asInt() {
            return null;
        }

        @Override
        public Long asLong() {
            return null;
        }

        @Override
        public Double asDouble() {
            return null;
        }

        @Override
        public String asString() {
            return null;
        }

        @Override
        public Date asDate() {
            return null;
        }

        @Override
        public <T> T[] asArray(Class<T> tClazz) throws JWTDecodeException {
            return null;
        }

        @Override
        public <T> List<T> asList(Class<T> tClazz) throws JWTDecodeException {
            return null;
        }

        @Override
        public Map<String, Object> asMap() throws JWTDecodeException {
            return null;
        }

        @Override
        public <T> T as(Class<T> tClazz) throws JWTDecodeException {
            return null;
        }
    }
}


