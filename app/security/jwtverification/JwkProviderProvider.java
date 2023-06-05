package security.jwtverification;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.typesafe.config.Config;

import javax.inject.Inject;
import javax.inject.Provider;

public class JwkProviderProvider implements Provider<JwkProvider> {
    private final Config config;

    @Inject
    public JwkProviderProvider(Config config) {
        this.config = config;
    }

    @Override
    public JwkProvider get() {
        String issuer = config.getString("galactic.host.jwt.issuer");
        return new UrlJwkProvider(issuer);
    }
}
