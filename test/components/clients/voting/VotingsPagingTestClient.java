package components.clients.voting;

import components.clients.TestClient;
import controllers.voting.routes;
import play.Application;
import play.mvc.Http;
import play.mvc.Result;

import static play.test.Helpers.GET;
import static play.test.Helpers.route;
import static utils.JwtTestUtils.addJwtTokenTo;

public class VotingsPagingTestClient extends TestClient {
    public VotingsPagingTestClient(Application application) {
        super(application);
    }

    public Result publicVotings(int offset, int limit) {
        String queryParams = String.format("offset=%d&limit=%d", offset, limit);

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.VotingsPagingController.publicVotings().url() + "?" + queryParams);

        return route(application, httpRequest);
    }

    public Result votingsOfVoteCaller(int offset, int limit, String userId) {
        String jwt = jwtTestUtils.createToken(userId, userId + "@mail.com");
        return votingsOfVoteCallerWithJwt(offset, limit, jwt);
    }

    public Result votingsOfVoteCaller(int offset, int limit, String userId, String[] roles) {
        String jwt = jwtTestUtils.createToken(userId, roles, userId + "@mail.com");
        return votingsOfVoteCallerWithJwt(offset, limit, jwt);
    }

    public Result votingsOfVoteCallerWithJwt(int offset, int limit, String jwt) {
        Http.RequestBuilder httpRequest = votingsOfVoteCallerBase(offset, limit);

        addJwtTokenTo(httpRequest, jwt);

        return route(application, httpRequest);
    }

    public Result votingsOfVoter(int offset, int limit, String userId) {
        String jwt = jwtTestUtils.createToken(userId, userId + "@mail.com");
        return votingsOfVoterWithRawJwt(offset, limit, jwt);
    }

    public Result votingsOfVoter(int offset, int limit, String userId, String[] roles) {
        Http.RequestBuilder httpRequest = votingsOfVoterBase(offset, limit);

        String jwt = jwtTestUtils.createToken(userId, roles, userId + "@mail.com");
        return votingsOfVoterWithRawJwt(offset, limit, jwt);
    }

    public Result votingsOfVoterWithRawJwt(int offset, int limit, String jwt) {
        Http.RequestBuilder httpRequest = votingsOfVoterBase(offset, limit);

        addJwtTokenTo(httpRequest, jwt);

        return route(application, httpRequest);
    }

    public Result votingsOfVoterFilteredByNotAlreadyTriedToVote(int offset, int limit, String userId) {
        String queryParams = String.format("offset=%d&limit=%d&filterByNotTriedToCastVote=true", offset, limit);

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(GET)
                .uri(routes.VotingsPagingController.votingsOfVoter().url() + "?" + queryParams);

        String jwt = jwtTestUtils.createToken(userId, userId + "@mail.com");
        addJwtTokenTo(httpRequest, jwt);

        return route(application, httpRequest);
    }

    private Http.RequestBuilder votingsOfVoterBase(int offset, int limit) {
        String queryParams = String.format("offset=%d&limit=%d", offset, limit);

        return new Http.RequestBuilder()
                .method(GET)
                .uri(routes.VotingsPagingController.votingsOfVoter().url() + "?" + queryParams);
    }

    private Http.RequestBuilder votingsOfVoteCallerBase(int offset, int limit) {
        String queryParams = String.format("offset=%d&limit=%d", offset, limit);

        return new Http.RequestBuilder()
                .method(GET)
                .uri(routes.VotingsPagingController.votingsOfVoteCaller().url() + "?" + queryParams);
    }
}
