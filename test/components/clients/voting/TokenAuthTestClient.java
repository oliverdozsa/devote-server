package components.clients.voting;

import components.clients.TestClient;
import controllers.voting.routes;
import play.Application;
import play.mvc.Http;
import play.mvc.Result;

import static play.test.Helpers.route;

public class TokenAuthTestClient extends TestClient {
    public TokenAuthTestClient(Application application) {
        super(application);
    }

    public Result auth(String token) {
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .uri(routes.TokenAuthController.auth(token).url());

        return route(application, httpRequest);
    }
}
