package security;

import com.typesafe.config.Config;

import javax.inject.Inject;

public class JwtMaker {
    @Inject
    public JwtMaker(Config config) {
        // TODO
    }

    public String create(Long votingId, String userId) {
        // TODO
        return null;
    }
}
