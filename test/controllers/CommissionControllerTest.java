package controllers;

import clients.CommissionTestClient;
import clients.VotingTestClient;
import ipfs.api.IpfsApi;
import ipfs.api.imp.MockIpfsApi;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import requests.CommissionAccountCreationRequest;
import requests.CommissionInitRequest;
import requests.CreateVotingRequest;
import rules.RuleChainForTests;
import services.Base62Conversions;
import utils.JwtTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;

import static asserts.DbAsserts.assertThatTransactionIsStoredFor;
import static controllers.VotingRequestMaker.createValidVotingRequest;
import static extractors.CommissionResponseFromResult.*;
import static extractors.GenericDataFromResult.statusOf;
import static matchers.ResultHasHeader.hasLocationHeader;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isEmptyString;
import static play.inject.Bindings.bind;
import static play.mvc.Http.HeaderNames.CONTENT_TYPE;
import static play.mvc.Http.HeaderNames.LOCATION;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;
import static utils.JwtTestUtils.addJwtTokenTo;

public class CommissionControllerTest {
    @Rule
    public RuleChain chain;

    private final RuleChainForTests ruleChainForTests;

    private CommissionTestClient testClient;
    private VotingTestClient votingTestClient;

    public CommissionControllerTest() {
        GuiceApplicationBuilder applicationBuilder = new GuiceApplicationBuilder()
                .overrides(bind(IpfsApi.class).to(MockIpfsApi.class));

        ruleChainForTests = new RuleChainForTests(applicationBuilder);
        chain = ruleChainForTests.getRuleChain();
    }

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

        assertThat(sessionJwtOf(result), notNullValue());
        assertThat(publicKeyOf(result), notNullValue());
    }

    @Test
    public void testJwtNotPresent() {
        // Given
        CommissionInitRequest initRequest = new CommissionInitRequest();
        initRequest.setVotingId("Some Voting");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(initRequest))
                .header(CONTENT_TYPE, Http.MimeTypes.JSON)
                .uri(routes.CommissionController.init().url());

        // When
        Result result = route(ruleChainForTests.getApplication(), httpRequest);

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    public void testJwtExpired() {
        // Given
        CommissionInitRequest initRequest = new CommissionInitRequest();
        initRequest.setVotingId("Some Voting");

        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .bodyJson(Json.toJson(initRequest))
                .header(CONTENT_TYPE, Http.MimeTypes.JSON)
                .uri(routes.CommissionController.init().url());

        JwtTestUtils jwtTestUtils = new JwtTestUtils(ruleChainForTests.getApplication().config());
        Date expiresAt = Date.from(Instant.now().minus(5, ChronoUnit.SECONDS));
        String jwt = jwtTestUtils.createToken(expiresAt, "Some user");
        addJwtTokenTo(httpRequest, jwt);

        // When
        Result result = route(ruleChainForTests.getApplication(), httpRequest);

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    public void testSignOnEnvelope() {
        // Given
        InitData votingInitData = initVotingFor("Bob");
        String message = createMessage(votingInitData.votingId, "someAccountId");

        // When
        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, votingInitData.sessionJwt, message);

        // Then
        assertThat(statusOf(result.http), equalTo(OK));
        assertThat(envelopeSignatureOf(result.http), notNullValue());
        assertThat(envelopeSignatureOf(result.http).length(), greaterThan(0));
    }

    @Test
    public void testDoubleEnvelope() {
        // Given
        InitData votingInitData = initVotingFor("Bob");
        String message = createMessage(votingInitData.votingId, "someAccountId");

        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, votingInitData.sessionJwt, message);
        assertThat(statusOf(result.http), equalTo(OK));
        assertThat(envelopeSignatureOf(result.http), notNullValue());
        assertThat(envelopeSignatureOf(result.http).length(), greaterThan(0));

        String newMessage = createMessage(votingInitData.votingId, "anotherAccountId");

        // When
        CommissionTestClient.SignOnEnvelopeResult newResult = testClient.signOnEnvelope(votingInitData.publicKey, votingInitData.sessionJwt, newMessage);

        // Then
        assertThat(statusOf(newResult.http), equalTo(FORBIDDEN));
    }

    @Test
    public void testInitFormError() {
        // Given
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .header(CONTENT_TYPE, Http.MimeTypes.JSON)
                .uri(routes.CommissionController.init().url());

        JwtTestUtils jwtTestUtils = new JwtTestUtils(ruleChainForTests.getApplication().config());
        String jwt = jwtTestUtils.createToken("Alice");
        addJwtTokenTo(httpRequest, jwt);

        // When
        Result result = route(ruleChainForTests.getApplication(), httpRequest);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    public void testSignEnvelopeFormError() {
        // Given
        Http.RequestBuilder httpRequest = new Http.RequestBuilder()
                .method(POST)
                .header(CONTENT_TYPE, Http.MimeTypes.JSON)
                .uri(routes.CommissionController.signEnvelope().url());

        JwtTestUtils jwtTestUtils = new JwtTestUtils(ruleChainForTests.getApplication().config());
        String jwt = jwtTestUtils.createToken("Alice");
        addJwtTokenTo(httpRequest, jwt);

        // When
        Result result = route(ruleChainForTests.getApplication(), httpRequest);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    // TODO: test for retrieving envelope signature for user in voting

    @Test
    public void testAccountCreationRequest() {
        // Given
        InitData votingInitData = initVotingFor("Bob");
        String message = createMessage(votingInitData.votingId, "someAccountId");

        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, votingInitData.sessionJwt, message);
        assertThat(statusOf(result.http), equalTo(OK));
        assertThat(envelopeSignatureOf(result.http), notNullValue());
        assertThat(envelopeSignatureOf(result.http).length(), greaterThan(0));

        // When
        String envelopeSignatureBase64 = envelopeSignatureOf(result.http);
        CommissionAccountCreationRequest accountCreationRequest = CommissionTestClient.createAccountCreationRequest(message, envelopeSignatureBase64, result.envelope);
        Result accountCreationRequestResult = testClient.requestAccountCreation(accountCreationRequest);

        // Then
        assertThat(statusOf(accountCreationRequestResult), equalTo(OK));
        assertThat(accountCreationPayloadOf(accountCreationRequestResult), notNullValue());
        assertThat(accountCreationPayloadOf(accountCreationRequestResult), not(isEmptyString()));
        assertThatTransactionIsStoredFor(accountCreationRequest.getRevealedSignatureBase64());
    }

    @Test
    public void testDoubleAccountCreationRequest() {
        // TODO: This should be forbidden. There should be a separate API for getting the public account key
        //       By presenting the original message and revealed signature.
        // Given
        // When
        // Then
    }

    @Test
    public void testGetAccountCreationTransaction() {
        // TODO: Present the same account creation request to obtain the stored transaction
    }

    @Test
    public void testGetAccountCreationTransaction_NotCreatedBefore() {
        // TODO
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
    }

    private InitData initVotingFor(String userId) {
        // Given
        String votingId = createValidVoting();
        CommissionInitRequest initRequest = new CommissionInitRequest();
        initRequest.setVotingId(votingId);

        // When
        Result result = testClient.init(initRequest, userId);

        // Then
        assertThat(statusOf(result), equalTo(OK));

        InitData initData = new InitData();
        initData.votingId = votingId;
        initData.publicKey = publicKeyOf(result);
        initData.sessionJwt = sessionJwtOf(result);

        return initData;
    }

    private String createMessage(String votingId, String voterPublicAccountId) {
        return votingId + "|" + voterPublicAccountId;
    }

    private static class InitData {
        public String votingId;
        public String publicKey;
        public String sessionJwt;
    }
}
