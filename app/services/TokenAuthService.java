package services;

import data.entities.JpaAuthToken;
import data.operations.TokenAuthDbOperations;
import responses.TokenAuthResponse;
import security.JwtCenter;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class TokenAuthService {
    private final TokenAuthDbOperations dbOperations;
    private final JwtCenter jwtCenter;

    @Inject
    public TokenAuthService(TokenAuthDbOperations dbOperations, JwtCenter jwtCenter) {
        this.dbOperations = dbOperations;
        this.jwtCenter = jwtCenter;
    }

    public CompletionStage<TokenAuthResponse> auth(String token) {
        return dbOperations.findBy(token)
                .thenApply(this::toResponse);
    }

    private TokenAuthResponse toResponse(JpaAuthToken jpaAuthToken) {
        String jwt = jwtCenter.createTokenAuthJwt(jpaAuthToken.getToken().toString());
        TokenAuthResponse response = new TokenAuthResponse();
        response.setToken(jwt);

        return response;
    }
}
