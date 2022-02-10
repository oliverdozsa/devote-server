package components.clients;

import controllers.routes;
import requests.CreateVotingRequest;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import static play.mvc.Http.HeaderNames.CONTENT_TYPE;
import static play.test.Helpers.*;

public class VotingTestClient extends TestClient {
    public VotingTestClient(Application application) {
        super(application);
    }

    public Result createVoting(CreateVotingRequest votingRequest) {
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(votingRequest))
                .header(CONTENT_TYPE, Http.MimeTypes.JSON)
                .uri(routes.VotingController.create().url());

        return route(application, httpRequest, 75 * 1000);
    }

    public Result single(String votingId) {
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.VotingController.single(votingId).url());

        return route(application, httpRequest);
    }
}
