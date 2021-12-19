package clients;

import controllers.routes;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import requests.CommissionInitRequest;
import utils.JwtTestUtils;

import static play.mvc.Http.HeaderNames.CONTENT_TYPE;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;
import static utils.JwtTestUtils.addJwtTokenTo;

public class CommissionTestClient extends TestClient {
    private final JwtTestUtils jwtTestUtils;

    public CommissionTestClient(Application application) {
        super(application);
        jwtTestUtils = new JwtTestUtils(application.config());
    }

    public Result init(CommissionInitRequest initRequest, String userId) {
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(initRequest))
                .header(CONTENT_TYPE, Http.MimeTypes.JSON)
                .uri(routes.CommissionController.init().url());

        String jwt = jwtTestUtils.createToken(userId);
        addJwtTokenTo(httpRequest, jwt);

        return route(application, httpRequest);
    }

    public Result signOnEnvelope(String sessionJwt, String envelope) {
        // TODO
        return null;
    }
}
