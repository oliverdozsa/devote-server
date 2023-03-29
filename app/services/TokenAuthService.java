package services;

import com.typesafe.config.Config;
import data.entities.JpaAuthToken;
import data.operations.TokenAuthDbOperations;
import responses.TokenAuthResponse;
import security.JwtCenter;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class TokenAuthService {
    private final TokenAuthDbOperations dbOperations;
    private final JwtCenter jwtCenter;
    private final String tokenAuthSubjectPrefix;

    @Inject
    public TokenAuthService(TokenAuthDbOperations dbOperations, JwtCenter jwtCenter, Config config) {
        this.dbOperations = dbOperations;
        this.jwtCenter = jwtCenter;
        tokenAuthSubjectPrefix = config.getString("galactic.vote.jwt.token.auth.subject.prefix");
    }

    public CompletionStage<TokenAuthResponse> auth(String token) {
        return dbOperations.findBy(token)
                .thenApply(this::toResponse);
    }

    private TokenAuthResponse toResponse(JpaAuthToken jpaAuthToken) {
        String userIdWithSubjectPrefix = tokenAuthSubjectPrefix + jpaAuthToken.getToken().toString();
        String jwt = jwtCenter.createTokenAuthJwt(userIdWithSubjectPrefix);
        TokenAuthResponse response = new TokenAuthResponse();
        response.setToken(jwt);

        return response;
    }
}
