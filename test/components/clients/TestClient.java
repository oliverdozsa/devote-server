package components.clients;

import play.Application;
import play.mvc.Http;
import play.mvc.Result;

import static play.mvc.Http.HttpVerbs.GET;
import static play.test.Helpers.route;

class TestClient {
    protected final Application application;

    public TestClient(Application application) {
        this.application = application;
    }

    public Result byLocation(String url) {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(url);

        return route(application, request);
    }
}
