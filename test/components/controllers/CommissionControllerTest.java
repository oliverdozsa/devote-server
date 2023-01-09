package components.controllers;

import components.clients.CommissionTestClient;
import components.clients.VotingTestClient;
import controllers.routes;
import data.entities.JpaCommissionSession;
import data.entities.JpaVoting;
import io.ebean.Ebean;
import io.ipfs.api.IPFS;
import ipfs.api.IpfsApi;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import requests.CommissionCreateTransactionRequest;
import requests.CommissionInitRequest;
import rules.RuleChainForTests;
import security.jwtverification.JwtVerificationForTests;
import security.jwtverification.JwtVerification;
import services.Base62Conversions;
import units.ipfs.api.imp.MockIpfsApi;
import units.ipfs.api.imp.MockIpfsProvider;
import utils.JwtTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static asserts.DbAsserts.assertThatTransactionIsStoredFor;
import static components.extractors.CommissionResponseFromResult.*;
import static components.extractors.GenericDataFromResult.statusOf;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isEmptyString;
import static play.inject.Bindings.bind;
import static play.mvc.Http.HeaderNames.CONTENT_TYPE;
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
    private VoteCreationUtils voteCreationUtils;

    public CommissionControllerTest() {
        GuiceApplicationBuilder applicationBuilder = new GuiceApplicationBuilder()
                .overrides(bind(IpfsApi.class).to(MockIpfsApi.class))
                .overrides(bind(IPFS.class).toProvider(MockIpfsProvider.class))
                .overrides((bind(JwtVerification.class).to(JwtVerificationForTests.class)));

        ruleChainForTests = new RuleChainForTests(applicationBuilder);
        chain = ruleChainForTests.getRuleChain();
    }

    @Before
    public void setup() {
        testClient = new CommissionTestClient(ruleChainForTests.getApplication());
        votingTestClient = new VotingTestClient(ruleChainForTests.getApplication());
        voteCreationUtils = new VoteCreationUtils(testClient, votingTestClient);
    }

    @Test
    public void testInit() throws InterruptedException {
        // Given
        String votingId = voteCreationUtils.createValidVotingWithWaitingForFullInit();
        CommissionInitRequest initRequest = new CommissionInitRequest();
        initRequest.setVotingId(votingId);

        // When
        Result result = testClient.init(initRequest, "Alice");

        // Then
        assertThat(statusOf(result), equalTo(OK));

        assertThat(publicKeyOf(result), notNullValue());
    }

    @Test
    public void testInitVotingIsNotInitializedProperly() {
        // Given
        String votingId = voteCreationUtils.createValidVoting();
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
        String jwt = jwtTestUtils.createToken(expiresAt, "Some user", "some@mail.com");
        addJwtTokenTo(httpRequest, jwt);

        // When
        Result result = route(ruleChainForTests.getApplication(), httpRequest);

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    public void testSignOnEnvelope() throws InterruptedException {
        // Given
        VoteCreationUtils.InitData votingInitData = voteCreationUtils.initVotingFor("Alice");
        String message = voteCreationUtils.createMessage(votingInitData.votingId, "someAccountId");

        // When
        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, "Alice", message, votingInitData.votingId);

        // Then
        assertThat(statusOf(result.http), equalTo(OK));
        assertThat(envelopeSignatureOf(result.http), notNullValue());
        assertThat(envelopeSignatureOf(result.http).length(), greaterThan(0));
    }

    @Test
    public void testDoubleEnvelope() throws InterruptedException {
        // Given
        VoteCreationUtils.InitData votingInitData = voteCreationUtils.initVotingFor("Alice");
        String message = voteCreationUtils.createMessage(votingInitData.votingId, "someAccountId");

        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, "Alice", message, votingInitData.votingId);
        assertThat(statusOf(result.http), equalTo(OK));
        assertThat(envelopeSignatureOf(result.http), notNullValue());
        assertThat(envelopeSignatureOf(result.http).length(), greaterThan(0));

        String newMessage = voteCreationUtils.createMessage(votingInitData.votingId, "anotherAccountId");

        // When
        CommissionTestClient.SignOnEnvelopeResult newResult = testClient.signOnEnvelope(votingInitData.publicKey, "Alice", newMessage, votingInitData.votingId);

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
        String jwt = jwtTestUtils.createToken("Alice", "alice@mail.com");
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
                .uri(routes.CommissionController.signEnvelope("42").url());

        JwtTestUtils jwtTestUtils = new JwtTestUtils(ruleChainForTests.getApplication().config());
        String jwt = jwtTestUtils.createToken("Alice", "alice@mail.com");
        addJwtTokenTo(httpRequest, jwt);

        // When
        Result result = route(ruleChainForTests.getApplication(), httpRequest);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    public void testCreateTransaction() throws InterruptedException {
        // Given
        VoteCreationUtils.InitData votingInitData = voteCreationUtils.initVotingFor("Alice");
        Thread.sleep(3 * 1000); // So that some channel accounts are present.
        String message = voteCreationUtils.createMessage(votingInitData.votingId, "someAccountId");

        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, "Alice", message, votingInitData.votingId);
        assertThat(statusOf(result.http), equalTo(OK));
        assertThat(envelopeSignatureOf(result.http), notNullValue());
        assertThat(envelopeSignatureOf(result.http).length(), greaterThan(0));

        // When
        String envelopeSignatureBase64 = envelopeSignatureOf(result.http);
        CommissionCreateTransactionRequest createTransactionRequest = CommissionTestClient.createTransactionCreationRequest(message, envelopeSignatureBase64, result.envelope);
        Result createTransactionRequestResult = testClient.requestAccountCreation(createTransactionRequest);

        // Then
        assertThat(statusOf(createTransactionRequestResult), equalTo(OK));
        assertThat(transactionOf(createTransactionRequestResult), notNullValue());
        assertThat(transactionOf(createTransactionRequestResult), not(isEmptyString()));
        assertThatTransactionIsStoredFor(createTransactionRequest.getRevealedSignatureBase64());
    }

    @Test
    public void testDoubleCreateTransactionRequest() throws InterruptedException {
        // Given
        VoteCreationUtils.InitData votingInitData = voteCreationUtils.initVotingFor("Alice");
        Thread.sleep(3 * 1000); // So that some channel accounts are present.
        String message = voteCreationUtils.createMessage(votingInitData.votingId, "someAccountId");

        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, "Alice", message, votingInitData.votingId);
        assertThat(statusOf(result.http), equalTo(OK));
        assertThat(envelopeSignatureOf(result.http), notNullValue());
        assertThat(envelopeSignatureOf(result.http).length(), greaterThan(0));

        String envelopeSignatureBase64 = envelopeSignatureOf(result.http);
        CommissionCreateTransactionRequest accountCreationRequest = CommissionTestClient.createTransactionCreationRequest(message, envelopeSignatureBase64, result.envelope);
        Result accountCreationRequestResult = testClient.requestAccountCreation(accountCreationRequest);

        assertThat(statusOf(accountCreationRequestResult), equalTo(OK));
        assertThat(transactionOf(accountCreationRequestResult), notNullValue());
        assertThat(transactionOf(accountCreationRequestResult), not(isEmptyString()));
        assertThatTransactionIsStoredFor(accountCreationRequest.getRevealedSignatureBase64());

        // When
        Result secondAccountCreationRequestResult = testClient.requestAccountCreation(accountCreationRequest);

        // Then
        assertThat(statusOf(secondAccountCreationRequestResult), equalTo(FORBIDDEN));
    }

    @Test
    public void testGetTransaction() throws InterruptedException {
        // Given
        VoteCreationUtils.InitData votingInitData = voteCreationUtils.initVotingFor("Alice");
        Thread.sleep(3 * 1000); // So that some channel accounts are present.
        String message = voteCreationUtils.createMessage(votingInitData.votingId, "someAccountId");

        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, "Alice", message, votingInitData.votingId);
        assertThat(statusOf(result.http), equalTo(OK));
        assertThat(envelopeSignatureOf(result.http), notNullValue());
        assertThat(envelopeSignatureOf(result.http).length(), greaterThan(0));

        String envelopeSignatureBase64 = envelopeSignatureOf(result.http);
        CommissionCreateTransactionRequest accountCreationRequest = CommissionTestClient.createTransactionCreationRequest(message, envelopeSignatureBase64, result.envelope);
        Result accountCreationRequestResult = testClient.requestAccountCreation(accountCreationRequest);

        assertThat(statusOf(accountCreationRequestResult), equalTo(OK));
        assertThat(transactionOf(accountCreationRequestResult), notNullValue());
        assertThat(transactionOf(accountCreationRequestResult), not(isEmptyString()));
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
        VoteCreationUtils.InitData votingInitData = voteCreationUtils.initVotingFor("Alice");
        String message = voteCreationUtils.createMessage(votingInitData.votingId, "someAccountId");

        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, "Alice", message, votingInitData.votingId);
        assertThat(statusOf(result.http), equalTo(OK));
        String envelopeSignatureOnSign = envelopeSignatureOf(result.http);

        assertThat(envelopeSignatureOnSign, notNullValue());
        assertThat(envelopeSignatureOf(result.http).length(), greaterThan(0));

        // When
        Result getEnvelopeSignatureResult = testClient.envelopeSignatureOf(votingInitData.votingId, "Alice");

        // Then
        assertThat(statusOf(getEnvelopeSignatureResult), equalTo(OK));
        assertThat(envelopeSignatureOf(getEnvelopeSignatureResult), notNullValue());

        String getEnvelopeSignature = envelopeSignatureOf(getEnvelopeSignatureResult);
        assertThat(getEnvelopeSignature, equalTo(envelopeSignatureOnSign));
    }

    @Test
    public void testGetEnvelopeSignatureForUserInVoting_NotSignedEnvelopeBefore() throws InterruptedException {
        // Given
        VoteCreationUtils.InitData votingInitData = voteCreationUtils.initVotingFor("Alice");

        // When
        Result getEnvelopeSignatureResult = testClient.envelopeSignatureOf(votingInitData.votingId, "Alice");

        // Then
        assertThat(statusOf(getEnvelopeSignatureResult), equalTo(NOT_FOUND));
    }

    @Test
    public void testSignOnEnvelopeWithoutInitBefore() throws InterruptedException {
        // Given
        VoteCreationUtils.InitData votingInitData = voteCreationUtils.initVotingFor("Alice");
        String message = voteCreationUtils.createMessage(votingInitData.votingId, "someAccountId");

        Ebean.createQuery(JpaCommissionSession.class)
                .delete();

        // When
        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, "Alice", message, votingInitData.votingId);

        // Then
        assertThat(statusOf(result.http), equalTo(NOT_FOUND));
    }

    @Test
    public void testInit_VotingEnded() throws InterruptedException {
        // Given
        String votingId = voteCreationUtils.createValidVotingWithWaitingForFullInit();
        CommissionInitRequest initRequest = new CommissionInitRequest();
        initRequest.setVotingId(votingId);

        endVoting(votingId);

        // When
        Result result = testClient.init(initRequest, "Alice");

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    public void testSignEnvelope_VotingEnded() throws InterruptedException {
        // Given
        VoteCreationUtils.InitData votingInitData = voteCreationUtils.initVotingFor("Alice");
        String message = voteCreationUtils.createMessage(votingInitData.votingId, "someAccountId");

        endVoting(votingInitData.votingId);

        // When
        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, "Alice", message, votingInitData.votingId);

        // Then
        assertThat(statusOf(result.http), equalTo(FORBIDDEN));
    }

    @Test
    public void testGetEnvelopeSignature_VotingEnded() throws InterruptedException {
        // Given
        VoteCreationUtils.InitData votingInitData = voteCreationUtils.initVotingFor("Alice");
        String message = voteCreationUtils.createMessage(votingInitData.votingId, "someAccountId");

        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, "Alice", message, votingInitData.votingId);
        assertThat(statusOf(result.http), equalTo(OK));
        String envelopeSignatureOnSign = envelopeSignatureOf(result.http);

        assertThat(envelopeSignatureOnSign, notNullValue());
        assertThat(envelopeSignatureOf(result.http).length(), greaterThan(0));

        endVoting(votingInitData.votingId);

        // When
        Result getEnvelopeSignatureResult = testClient.envelopeSignatureOf(votingInitData.votingId, "Alice");

        // Then
        assertThat(statusOf(getEnvelopeSignatureResult), equalTo(FORBIDDEN));
    }

    @Test
    public void testInit_VotingNotStartedYet() throws InterruptedException {
        // Given
        String votingId = voteCreationUtils.createValidVotingWithWaitingForFullInit();
        CommissionInitRequest initRequest = new CommissionInitRequest();
        initRequest.setVotingId(votingId);

        startVotingInFuture(votingId);

        // When
        Result result = testClient.init(initRequest, "Alice");

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    public void testSignEnvelope_VotingNotStartedYet() throws InterruptedException {
        // Given
        VoteCreationUtils.InitData votingInitData = voteCreationUtils.initVotingFor("Alice");
        String message = voteCreationUtils.createMessage(votingInitData.votingId, "someAccountId");

        startVotingInFuture(votingInitData.votingId);

        // When
        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, "Alice", message, votingInitData.votingId);

        // Then
        assertThat(statusOf(result.http), equalTo(FORBIDDEN));
    }

    @Test
    public void testGetEnvelopeSignature_VotingNotStartedYet() throws InterruptedException {
        // Given
        VoteCreationUtils.InitData votingInitData = voteCreationUtils.initVotingFor("Alice");
        String message = voteCreationUtils.createMessage(votingInitData.votingId, "someAccountId");

        CommissionTestClient.SignOnEnvelopeResult result = testClient.signOnEnvelope(votingInitData.publicKey, "Alice", message, votingInitData.votingId);
        assertThat(statusOf(result.http), equalTo(OK));
        String envelopeSignatureOnSign = envelopeSignatureOf(result.http);

        assertThat(envelopeSignatureOnSign, notNullValue());
        assertThat(envelopeSignatureOf(result.http).length(), greaterThan(0));

        startVotingInFuture(votingInitData.votingId);

        // When
        Result getEnvelopeSignatureResult = testClient.envelopeSignatureOf(votingInitData.votingId, "Alice");

        // Then
        assertThat(statusOf(getEnvelopeSignatureResult), equalTo(FORBIDDEN));
    }

    private void endVoting(String votingIdEncoded) {
        long votingId = Base62Conversions.decode(votingIdEncoded);
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);
        voting.setEndDate(Instant.now().minus(Duration.ofSeconds(15)));
        Ebean.update(voting);
    }

    private void startVotingInFuture(String votingIdEncoded) {
        long votingId = Base62Conversions.decode(votingIdEncoded);
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);
        voting.setStartDate(Instant.now().plus(Duration.ofSeconds(15)));
        Ebean.update(voting);
    }
}
