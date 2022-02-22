package units.security.jwtverification;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import security.jwtverification.Auth0JwtVerification;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class Auth0JwtVerificationTest {
    @Mock
    private Config mockConfig;

    @Mock
    private JwkProvider mockJwkProvider;

    @Mock
    private Jwk mockJwk;

    private final String mockIssuer = "mock-issuer";
    private final KeyPair testKeyPair = createKeyPair();

    @Before
    public void setup() throws JwkException {
        MockitoAnnotations.initMocks(this);

        when(mockConfig.getString(anyString())).thenReturn(mockIssuer);
        when(mockJwkProvider.get(anyString())).thenReturn(mockJwk);
        when(mockJwk.getPublicKey()).thenReturn(testKeyPair.getPublic());
    }

    @Test
    public void testVerificationSucceeds() throws NoSuchAlgorithmException {
        // Given
        String jwt = createTestJwt("mock-issuer", "John");
        Auth0JwtVerification auth0JwtVerification = new Auth0JwtVerification(mockConfig, mockJwkProvider);

        // When
        DecodedJWT decodedJWT = auth0JwtVerification.verify(jwt);

        // Then
        assertThat(decodedJWT, notNullValue());
    }

    @Test
    public void testJwkProviderFails() throws JwkException {
        // Given
        when(mockJwkProvider.get(anyString())).thenThrow(new JwkException("someJwkError"));
        String jwt = createTestJwt("wrongIssuer", "John");
        Auth0JwtVerification auth0JwtVerification = new Auth0JwtVerification(mockConfig, mockJwkProvider);

        // When
        // Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> auth0JwtVerification.verify(jwt));
        assertThat(exception.getMessage(), containsString("Failed to verify jwt "));
    }

    private KeyPair createKeyPair() {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String createTestJwt(String issuer, String user) {
        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) testKeyPair.getPublic(), (RSAPrivateKey) testKeyPair.getPrivate());

        return JWT.create()
                .withIssuer(issuer)
                .withExpiresAt(Date.from(Instant.now().plus(Duration.ofMinutes(10))))
                .withSubject(user)
                .withKeyId("someKey")
                .sign(algorithm);
    }
}
