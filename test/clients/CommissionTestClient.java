package clients;

import controllers.routes;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import requests.CommissionInitRequest;

import static play.mvc.Http.HeaderNames.CONTENT_TYPE;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;

public class CommissionTestClient extends TestClient{
    public CommissionTestClient(Application application) {
        super(application);
    }

    public Result init(CommissionInitRequest initRequest) {
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(initRequest))
                .header(CONTENT_TYPE, Http.MimeTypes.JSON)
                .uri(routes.CommissionController.init().url());

        return route(application, httpRequest);
    }
}
