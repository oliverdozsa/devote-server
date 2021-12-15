package controllers;

import clients.CommissionTestClient;
import clients.VotingTestClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import requests.CommissionInitRequest;
import requests.CreateVotingRequest;
import rules.RuleChainForTests;

import java.util.Arrays;

import static controllers.VotingRequestMaker.createValidVotingRequest;
import static extractors.CommissionInitResponseFromResult.publicKeyOf;
import static extractors.GenericDataFromResult.jsonOf;
import static extractors.GenericDataFromResult.statusOf;
import static matchers.ResultHasHeader.hasLocationHeader;
import static matchers.ResultHasHeader.hasSessionTokenHeader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static play.mvc.Http.HeaderNames.LOCATION;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.OK;

public class CommissionControllerTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private CommissionTestClient testClient;
    private VotingTestClient votingTestClient;

    @Before
    public void setup() {
        testClient = new CommissionTestClient(ruleChainForTests.getApplication());
        votingTestClient = new VotingTestClient(ruleChainForTests.getApplication());
    }

    @Test
    public void testInit() {
        // Given
        String votingId = createValidVoting();
        CommissionInitRequest initRequest = new CommissionInitRequest();
        initRequest.setVotingId(votingId);

        // When
        Result result = testClient.init(initRequest, "Alice");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(result, hasSessionTokenHeader());

        assertThat(publicKeyOf(result), notNullValue());
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
