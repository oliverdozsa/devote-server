package clients;

import controllers.routes;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import requests.CastVoteInitRequest;

import static play.mvc.Http.HeaderNames.CONTENT_TYPE;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;

public class CastVoteTestClient extends TestClient{
    public CastVoteTestClient(Application application) {
        super(application);
    }

    public Result init(CastVoteInitRequest initRequest) {
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(initRequest))
                .header(CONTENT_TYPE, Http.MimeTypes.JSON)
                .uri(routes.CastVoteController.init().url());

        return route(application, httpRequest);
    }
}
