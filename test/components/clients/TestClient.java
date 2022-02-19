package components.clients;

import play.Application;
import play.mvc.Http;
import play.mvc.Result;
import security.UserInfoCollectorForTest;
import utils.JwtTestUtils;

import static play.mvc.Http.HttpVerbs.GET;
import static play.test.Helpers.route;
import static utils.JwtTestUtils.addJwtTokenTo;

class TestClient {
    protected final Application application;
    protected final JwtTestUtils jwtTestUtils;

    public TestClient(Application application) {
        this.application = application;
        jwtTestUtils = new JwtTestUtils(application.config());
    }

    public Result byLocation(String url) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(url);

        return route(application, request);
    }

    public Result byLocation(String url, String userId, String[] roles) {
        UserInfoCollectorForTest.setReturnValueFor(userId);
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(url);

        String jwt = jwtTestUtils.createToken(userId, roles);
        addJwtTokenTo(request, jwt);

        return route(application, request);
    }
}
