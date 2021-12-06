package controllers;

import clients.CastVoteTestClient;
import clients.VotingTestClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import requests.CastVoteInitRequest;
import requests.CreateVotingRequest;
import rules.RuleChainForTests;

import java.util.Arrays;

import static controllers.VotingRequestMaker.createValidVotingRequest;
import static extractors.GenericDataFromResult.statusOf;
import static matchers.ResultHasHeader.hasLocationHeader;
import static matchers.ResultHasHeader.hasSessionTokenHeader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static play.mvc.Http.HeaderNames.LOCATION;
import static play.mvc.Http.Status.CREATED;

public class CastVoteControllerTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private CastVoteTestClient testClient;
    private VotingTestClient votingTestClient;

    @Before
    public void setup() {
        testClient = new CastVoteTestClient(ruleChainForTests.getApplication());
        votingTestClient = new VotingTestClient(ruleChainForTests.getApplication());
    }

    @Test
    public void testInit() {
        // Given
        String votingId = createValidVoting();
        CastVoteInitRequest initRequest = new CastVoteInitRequest();
        initRequest.setVotingId(votingId);

        // When
        Result result = testClient.init(initRequest);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasSessionTokenHeader());
        // TODO: Other asserts
    }

    private String createValidVoting() {
        CreateVotingRequest createVotingRequest = createValidVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.EMAILS);
        createVotingRequest.setAuthorizationEmailOptions(Arrays.asList("john@mail.com", "doe@where.de", "some@one.com"));
        Result result = votingTestClient.createVoting(createVotingRequest);
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String locationUrl = result.headers().get(LOCATION);
        String[] locationUrlParts = locationUrl.split("/");
        String votingId = locationUrlParts[locationUrlParts.length - 1];

        return votingId;
        // TODO: some sleep might be needed for channel accounts to be present
    }
}
