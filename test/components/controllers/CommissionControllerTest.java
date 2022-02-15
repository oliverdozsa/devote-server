package components.controllers;

import components.clients.CommissionTestClient;
import components.clients.VotingTestClient;
import controllers.routes;
import io.ipfs.api.IPFS;
import ipfs.api.IpfsApi;
import services.Base62Conversions;
import units.ipfs.api.imp.MockIpfsApi;
import units.ipfs.api.imp.MockIpfsProvider;
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
import utils.JwtTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;

import static asserts.DbAsserts.assertThatTransactionIsStoredFor;
import static components.controllers.VotingRequestMaker.createValidVotingRequest;
import static components.extractors.CommissionResponseFromResult.*;
import static components.extractors.GenericDataFromResult.statusOf;
import static matchers.ResultHasHeader.hasLocationHeader;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.fail;
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
                .overrides(bind(IpfsApi.class).to(MockIpfsApi.class))
                .overrides(bind(IPFS.class).toProvider(MockIpfsProvider.class));

        ruleChainForTests = new RuleChainForTests(applicationBuilder);
        chain = ruleChainForTests.getRuleChain();
    }

    @Before
    public void setup() {
        testClient = new CommissionTestClient(ruleChainForTests.getApplication());
        votingTestClient = new VotingTestClient(ruleChainForTests.getApplication());
    }

    @Test
    public void testInit() throws InterruptedException {
        // Given
        String votingId = createValidVotingWithWaitingForFullInit();
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
    public void testInitVotingIsNotInitializedProperly() throws InterruptedException {
        // Given
        String votingId = createValidVoting();
        CommissionInitRequest initRequest = new CommissionInitRequest();
        initRequest.setVotingId(votingId);

        // When
        Result result = testClient.init(initRequest, "Alice");

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
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
    public void testSignOnEnvelope() throws InterruptedException {
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
    public void testDoubleEnvelope() throws InterruptedException {
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

    @Test
    public void testAccountCreationRequest() throws InterruptedException {
        // Given
        InitData votingInitData = initVotingFor("Bob");
        Thread.sleep(15 * 1000); // So that some channel accounts are present.
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
        assertThat(accountCreationTransactionOf(accountCreationRequestResult), notNullValue());
        assertThat(accountCreationTransactionOf(accountCreationRequestResult), not(isEmptyString()));
        assertThatTransactionIsStoredFor(accountCreationRequest.getRevealedSignatureBase64());
    }

    @Test
    public void testDoubleAccountCreationRequest() throws InterruptedException {
        // Given
        InitData votingInitData = initVotingFor("Bob");
        Thread.sleep(15 * 1000); // So that some channel accounts are present.
        String message = createMessage(votingInitData.votingId, "someAccountId");

        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, votingInitData.sessionJwt, message);
        assertThat(statusOf(result.http), equalTo(OK));
        assertThat(envelopeSignatureOf(result.http), notNullValue());
        assertThat(envelopeSignatureOf(result.http).length(), greaterThan(0));

        String envelopeSignatureBase64 = envelopeSignatureOf(result.http);
        CommissionAccountCreationRequest accountCreationRequest = CommissionTestClient.createAccountCreationRequest(message, envelopeSignatureBase64, result.envelope);
        Result accountCreationRequestResult = testClient.requestAccountCreation(accountCreationRequest);

        assertThat(statusOf(accountCreationRequestResult), equalTo(OK));
        assertThat(accountCreationTransactionOf(accountCreationRequestResult), notNullValue());
        assertThat(accountCreationTransactionOf(accountCreationRequestResult), not(isEmptyString()));
        assertThatTransactionIsStoredFor(accountCreationRequest.getRevealedSignatureBase64());

        // When
        Result secondAccountCreationRequestResult = testClient.requestAccountCreation(accountCreationRequest);

        // Then
        assertThat(statusOf(secondAccountCreationRequestResult), equalTo(FORBIDDEN));
    }

    @Test
    public void testGetAccountCreationTransaction() throws InterruptedException {
        // Given
        InitData votingInitData = initVotingFor("Bob");
        Thread.sleep(15 * 1000); // So that some channel accounts are present.
        String message = createMessage(votingInitData.votingId, "someAccountId");

        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, votingInitData.sessionJwt, message);
        assertThat(statusOf(result.http), equalTo(OK));
        assertThat(envelopeSignatureOf(result.http), notNullValue());
        assertThat(envelopeSignatureOf(result.http).length(), greaterThan(0));

        String envelopeSignatureBase64 = envelopeSignatureOf(result.http);
        CommissionAccountCreationRequest accountCreationRequest = CommissionTestClient.createAccountCreationRequest(message, envelopeSignatureBase64, result.envelope);
        Result accountCreationRequestResult = testClient.requestAccountCreation(accountCreationRequest);

        assertThat(statusOf(accountCreationRequestResult), equalTo(OK));
        assertThat(accountCreationTransactionOf(accountCreationRequestResult), notNullValue());
        assertThat(accountCreationTransactionOf(accountCreationRequestResult), not(isEmptyString()));
        assertThatTransactionIsStoredFor(accountCreationRequest.getRevealedSignatureBase64());

        // When
        Result transactionOfSignatureResult = testClient.transactionOfSignature(accountCreationRequest.getRevealedSignatureBase64());

        // Then
        assertThat(statusOf(transactionOfSignatureResult), equalTo(OK));
        assertThat(transactionOfSignature(transactionOfSignatureResult), notNullValue());
    }

    @Test
    public void testGetAccountCreationTransaction_NotCreatedBefore() {
        // Given
        String someRandomSignature = "844221";

        // When
        Result transactionOfSignatureResult = testClient.transactionOfSignature(someRandomSignature);

        // Then
        assertThat(statusOf(transactionOfSignatureResult), equalTo(NOT_FOUND));
    }

    @Test
    public void testGetEnvelopeSignatureForUserInVoting() throws InterruptedException {
        // Given
        InitData votingInitData = initVotingFor("Bob");
        String message = createMessage(votingInitData.votingId, "someAccountId");

        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, votingInitData.sessionJwt, message);
        assertThat(statusOf(result.http), equalTo(OK));
        String envelopeSignatureOnSign = envelopeSignatureOf(result.http);

        assertThat(envelopeSignatureOnSign, notNullValue());
        assertThat(envelopeSignatureOf(result.http).length(), greaterThan(0));

        // When
        Result getEnvelopeSignatureResult = testClient.envelopeSignatureOf(votingInitData.votingId, "Bob", votingInitData.sessionJwt);

        // Then
        assertThat(statusOf(getEnvelopeSignatureResult), equalTo(OK));
        assertThat(envelopeSignatureOf(getEnvelopeSignatureResult), notNullValue());

        String getEnvelopeSignature = envelopeSignatureOf(getEnvelopeSignatureResult);
        assertThat(getEnvelopeSignature, equalTo(envelopeSignatureOnSign));
    }

    @Test
    public void testGetEnvelopeSignatureForUserInVoting_NotSignedEnvelopeBefore() throws InterruptedException {
        // Given
        InitData votingInitData = initVotingFor("Bob");

        // When
        Result getEnvelopeSignatureResult = testClient.envelopeSignatureOf(votingInitData.votingId, "Bob", votingInitData.sessionJwt);

        // Then
        assertThat(statusOf(getEnvelopeSignatureResult), equalTo(NOT_FOUND));
    }

    @Test
    public void testEncryptOptionCodeForVoting() throws InterruptedException {
        // Given
        String votingId = createValidVoting();

        // When
        int anOptionCode = 42;
        Result anEncryptedOptionResult = testClient.encryptOptionCode(votingId, anOptionCode);
        Result anotherEncryptedOptionResult = testClient.encryptOptionCode(votingId, anOptionCode);

        // Then
        assertThat(statusOf(anEncryptedOptionResult), equalTo(OK));
        assertThat(statusOf(anotherEncryptedOptionResult), equalTo(OK));

        String firstEncryptedOptionCode = encryptedOptionCodeOf(anEncryptedOptionResult);
        String secondEncryptedOptionCode = encryptedOptionCodeOf(anotherEncryptedOptionResult);

        assertThat(firstEncryptedOptionCode, notNullValue());
        assertThat(firstEncryptedOptionCode.length(), greaterThan(0));
        assertThat(secondEncryptedOptionCode, notNullValue());
        assertThat(secondEncryptedOptionCode.length(), greaterThan(0));
        assertThat(firstEncryptedOptionCode, not(equalTo(secondEncryptedOptionCode)));
    }

    @Test
    public void testEncryptOptionCodeForVoting_InvalidOptionCode() throws InterruptedException {
        // Given
        String votingId = createValidVoting();

        // When
        int anOptionCode = 4200;
        Result anEncryptedOptionResult = testClient.encryptOptionCode(votingId, anOptionCode);

        // Then
        assertThat(statusOf(anEncryptedOptionResult), equalTo(BAD_REQUEST));

    }

    @Test
    public void testEncryptOptionCodeForVoting_VotingDoesNotExist() throws InterruptedException {
        // Given
        createValidVoting();

        // When
        int anOptionCode = 42;
        Result anEncryptedOptionResult = testClient.encryptOptionCode(Base62Conversions.encode(42L), anOptionCode);

        // Then
        assertThat(statusOf(anEncryptedOptionResult), equalTo(NOT_FOUND));
    }

    @Test
    public void testEncryptOptionCodeForVoting_VotingIsNotEncrypted() throws InterruptedException {
        // Given
        String votingId = createValidNotEncyptedVoting();

        // When
        int anOptionCode = 42;
        Result anEncryptedOptionResult = testClient.encryptOptionCode(votingId, anOptionCode);


        // Then
        assertThat(statusOf(anEncryptedOptionResult), equalTo(BAD_REQUEST));
    }

    private String createValidVotingWithWaitingForFullInit() throws InterruptedException {
        String votingId = createValidVoting();
        Thread.sleep(8 * 1000);
        return votingId;
    }

    private String createValidVoting() {
        CreateVotingRequest createVotingRequest = createValidVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.EMAILS);
        createVotingRequest.setAuthorizationEmailOptions(Arrays.asList("john@mail.com", "doe@where.de", "some@one.com"));

        return createVoting(createVotingRequest);
    }

    private String createValidNotEncyptedVoting() {
        CreateVotingRequest createVotingRequest = createValidVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.EMAILS);
        createVotingRequest.setAuthorizationEmailOptions(Arrays.asList("john@mail.com", "doe@where.de", "some@one.com"));
        createVotingRequest.setEncryptedUntil(null);

        return createVoting(createVotingRequest);
    }

    private String createVoting(CreateVotingRequest createVotingRequest) {
        Result result = votingTestClient.createVoting(createVotingRequest);
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String locationUrl = result.headers().get(LOCATION);
        String[] locationUrlParts = locationUrl.split("/");
        String votingId = locationUrlParts[locationUrlParts.length - 1];

        return votingId;
    }

    private InitData initVotingFor(String userId) throws InterruptedException {
        // Given
        String votingId = createValidVotingWithWaitingForFullInit();
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
