package components.controllers;

import components.clients.CommissionTestClient;
import components.clients.TokenAuthTestClient;
import components.clients.VotingTestClient;
import components.clients.VotingsPagingTestClient;
import data.entities.JpaAuthToken;
import data.entities.JpaVoting;
import io.ebean.Ebean;
import io.ipfs.api.IPFS;
import ipfs.api.IpfsApi;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import requests.CommissionCreateTransactionRequest;
import requests.CommissionInitRequest;
import requests.CreateVotingRequest;
import rules.RuleChainForTests;
import security.jwtverification.JwtVerification;
import security.jwtverification.JwtVerificationForTests;
import services.Base62Conversions;
import units.ipfs.api.imp.MockIpfsApi;
import units.ipfs.api.imp.MockIpfsProvider;
import utils.JwtTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import static asserts.DbAsserts.assertThatTransactionIsStoredFor;
import static components.controllers.VotingRequestMaker.createValidVotingRequest;
import static components.extractors.CommissionResponseFromResult.*;
import static components.extractors.GenericDataFromResult.statusOf;
import static components.extractors.TokenAuthResponseFromResult.jwtOf;
import static components.extractors.VotingPagingItemResponsesFromResult.votingIdsOf;
import static components.extractors.VotingResponseFromResult.titleOf;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isEmptyString;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.*;

public class TokenAuthVotingTest {
    @Rule
    public RuleChain chain;

    private final RuleChainForTests ruleChainForTests;

    private CommissionTestClient commissionTestClient;
    private VotingTestClient votingTestClient;
    private VoteCreationUtils voteCreationUtils;
    private TokenAuthTestClient tokenAuthTestClient;
    private VotingsPagingTestClient votingsPagingTestClient;

    private String authTokenForAliceForFirstVoting;
    private String votingIdOfFirst;
    private String votingIdOfSecond;

    public TokenAuthVotingTest() {
        GuiceApplicationBuilder applicationBuilder = new GuiceApplicationBuilder()
                .overrides(bind(IpfsApi.class).to(MockIpfsApi.class))
                .overrides(bind(IPFS.class).toProvider(MockIpfsProvider.class))
                .overrides((bind(JwtVerification.class).qualifiedWith("auth0").to(JwtVerificationForTests.class)));

        ruleChainForTests = new RuleChainForTests(applicationBuilder);
        chain = ruleChainForTests.getRuleChain();
    }

    @Before
    public void setup() throws InterruptedException {
        tokenAuthTestClient = new TokenAuthTestClient(ruleChainForTests.getApplication());
        commissionTestClient = new CommissionTestClient(ruleChainForTests.getApplication());
        votingTestClient = new VotingTestClient(ruleChainForTests.getApplication());
        votingsPagingTestClient = new VotingsPagingTestClient(ruleChainForTests.getApplication());

        voteCreationUtils = new VoteCreationUtils(commissionTestClient, votingTestClient);

        setupVotingsAndToken();
    }

    @Test
    public void testAccessingSingleVoteViaToken() {
        // Given
        Result result = tokenAuthTestClient.auth(authTokenForAliceForFirstVoting);
        assertThat(statusOf(result), equalTo(OK));

        String jwt = jwtOf(result);

        // When
        Result singleVotingResult = votingTestClient.single(votingIdOfFirst, jwt);

        // Then
        assertThat(statusOf(singleVotingResult), equalTo(OK));
        assertThat(titleOf(singleVotingResult), equalTo("First voting"));
    }

    @Test
    public void testAccessingSingleVoteViaTokenButItIsForOtherVoting() {
        // Given
        Result result = tokenAuthTestClient.auth(authTokenForAliceForFirstVoting);
        assertThat(statusOf(result), equalTo(OK));

        String jwt = jwtOf(result);

        // When
        Result singleVotingResult = votingTestClient.single(votingIdOfSecond, jwt);

        // Then
        assertThat(statusOf(singleVotingResult), equalTo(FORBIDDEN));
    }

    @Test
    public void testAccessingVotingsViaToken() {
        // Given
        Result result = tokenAuthTestClient.auth(authTokenForAliceForFirstVoting);
        assertThat(statusOf(result), equalTo(OK));

        String jwt = jwtOf(result);

        // When
        Result votingsResult = votingsPagingTestClient.votingsOfVoterWithRawJwt(0, 10, jwt);

        // Then
        assertThat(statusOf(votingsResult), equalTo(OK));
        assertThat(votingIdsOf(votingsResult).size(), Matchers.equalTo(1));
        assertThat(votingIdsOf(votingsResult).get(0), equalTo(votingIdOfFirst));
    }

    @Test
    public void testAccessingVotingsViaAuthAndTokenExistsForEmail() {
        // Given
        // When
        Result result = votingsPagingTestClient.votingsOfVoter(0, 10, "Alice");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(votingIdsOf(result).size(), Matchers.equalTo(2));
    }

    @Test
    public void testAccessingSingleVotingViaAuthTokenExistsForEmail() {
        // Given
        String jwt = new JwtTestUtils(ruleChainForTests.getApplication().config())
                .createToken("Alice", new String[]{"voter"}, "alice@mail.com");

        // When
        Result singleVotingResult = votingTestClient.single(votingIdOfFirst, jwt);

        // Then
        assertThat(statusOf(singleVotingResult), equalTo(OK));
        assertThat(titleOf(singleVotingResult), equalTo("First voting"));
    }

    @Test
    public void createTransactionViaToken() {
        // Given
        Result result = tokenAuthTestClient.auth(authTokenForAliceForFirstVoting);
        assertThat(statusOf(result), equalTo(OK));

        String jwt = jwtOf(result);

        // When
        // Then

        // Init
        CommissionInitRequest initRequest = new CommissionInitRequest();
        initRequest.setVotingId(votingIdOfFirst);

        Result initResult = commissionTestClient.initWithJwt(initRequest, jwt);

        assertThat(statusOf(initResult), equalTo(OK));

        VoteCreationUtils.InitData initData = new VoteCreationUtils.InitData();
        initData.votingId = votingIdOfFirst;
        initData.publicKey = publicKeyOf(initResult);

        // Sign envelope
        String message = voteCreationUtils.createMessage(votingIdOfFirst, "someAccountId");

        CommissionTestClient.SignOnEnvelopeResult signOnEnvelopeResult =
                commissionTestClient.signOnEnvelopeWithJwt(initData.publicKey, jwt, message, initData.votingId);
        assertThat(statusOf(signOnEnvelopeResult.http), equalTo(OK));
        assertThat(envelopeSignatureOf(signOnEnvelopeResult.http), notNullValue());
        assertThat(envelopeSignatureOf(signOnEnvelopeResult.http).length(), greaterThan(0));

        String envelopeSignatureBase64 = envelopeSignatureOf(signOnEnvelopeResult.http);

        Result envelopeSignatureResult = commissionTestClient.envelopeSignatureOfWithJwt(votingIdOfFirst, jwt);
        assertThat(statusOf(envelopeSignatureResult), equalTo(OK));

        // Create transaction
        CommissionCreateTransactionRequest createTransactionRequest = CommissionTestClient.createTransactionCreationRequest(message, envelopeSignatureBase64, signOnEnvelopeResult.envelope);
        Result createTransactionRequestResult = commissionTestClient.requestAccountCreation(createTransactionRequest);

        assertThat(statusOf(createTransactionRequestResult), equalTo(OK));
        assertThat(transactionOf(createTransactionRequestResult), notNullValue());
        assertThat(transactionOf(createTransactionRequestResult), not(isEmptyString()));
        assertThatTransactionIsStoredFor(createTransactionRequest.getRevealedSignatureBase64());
    }

    @Test
    public void testCreateTransactionThroughAuthAndTokenExistsForEmail() {
        // Given
        // When
        // Then

        // Init
        CommissionInitRequest initRequest = new CommissionInitRequest();
        initRequest.setVotingId(votingIdOfFirst);

        Result initResult = commissionTestClient.init(initRequest, "Alice");

        assertThat(statusOf(initResult), equalTo(OK));

        VoteCreationUtils.InitData initData = new VoteCreationUtils.InitData();
        initData.votingId = votingIdOfFirst;
        initData.publicKey = publicKeyOf(initResult);

        // Sign envelope
        String message = voteCreationUtils.createMessage(votingIdOfFirst, "someAccountId");

        CommissionTestClient.SignOnEnvelopeResult signOnEnvelopeResult =
                commissionTestClient.signOnEnvelope(initData.publicKey, "Alice", message, initData.votingId);
        assertThat(statusOf(signOnEnvelopeResult.http), equalTo(OK));
        assertThat(envelopeSignatureOf(signOnEnvelopeResult.http), notNullValue());
        assertThat(envelopeSignatureOf(signOnEnvelopeResult.http).length(), greaterThan(0));

        String envelopeSignatureBase64 = envelopeSignatureOf(signOnEnvelopeResult.http);

        // Create transaction
        CommissionCreateTransactionRequest createTransactionRequest = CommissionTestClient.createTransactionCreationRequest(message, envelopeSignatureBase64, signOnEnvelopeResult.envelope);
        Result createTransactionRequestResult = commissionTestClient.requestAccountCreation(createTransactionRequest);

        assertThat(statusOf(createTransactionRequestResult), equalTo(OK));
        assertThat(transactionOf(createTransactionRequestResult), notNullValue());
        assertThat(transactionOf(createTransactionRequestResult), not(isEmptyString()));
        assertThatTransactionIsStoredFor(createTransactionRequest.getRevealedSignatureBase64());
    }

    @Test
    public void testAccessingVotingsViaToken_TokenExpired() throws InterruptedException {
        // Given
        Result result = tokenAuthTestClient.auth(authTokenForAliceForFirstVoting);
        assertThat(statusOf(result), equalTo(OK));

        String jwt = jwtOf(result);

        expireVotingBy40Days(votingIdOfFirst);
        waitForTokenCleanUp();

        // When
        Result votingsResult = votingsPagingTestClient.votingsOfVoterWithRawJwt(0, 10, jwt);

        // Then
        assertThat(statusOf(votingsResult), equalTo(NOT_FOUND));
    }

    @Test
    public void testTokenExpired() throws InterruptedException {
        // Given
        expireVotingBy40Days(votingIdOfFirst);
        waitForTokenCleanUp();

        // When
        Result result = tokenAuthTestClient.auth(authTokenForAliceForFirstVoting);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    public void testAccessingSingleVoteViaToken_TokenExpired() throws InterruptedException {
        // Given
        Result result = tokenAuthTestClient.auth(authTokenForAliceForFirstVoting);
        assertThat(statusOf(result), equalTo(OK));

        String jwt = jwtOf(result);

        expireVotingBy40Days(votingIdOfFirst);
        waitForTokenCleanUp();

        // When
        Result singleVotingResult = votingTestClient.single(votingIdOfFirst, jwt);

        // Then
        assertThat(statusOf(singleVotingResult), equalTo(FORBIDDEN));
    }

    @Test
    public void testTokenNotFound() {
        // Given
        // When
        Result result = tokenAuthTestClient.auth("someRandomAuthToken");

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    public void testCreateVotingIsForbiddenViaToken() {
        // Given
        Result result = tokenAuthTestClient.auth(authTokenForAliceForFirstVoting);
        assertThat(statusOf(result), equalTo(OK));

        String jwt = jwtOf(result);

        // When
        CreateVotingRequest createVotingRequest = createValidVotingRequest();
        Result createVotingResult = votingTestClient.createVoting(createVotingRequest, jwt);

        // Then
        assertThat(statusOf(createVotingResult), equalTo(FORBIDDEN));
    }

    @Test
    public void testAccessingVotingsAsVoteCallerViaToken() {
        // Given
        Result result = tokenAuthTestClient.auth(authTokenForAliceForFirstVoting);
        assertThat(statusOf(result), equalTo(OK));

        String jwt = jwtOf(result);

        // When
        Result votingsResult = votingsPagingTestClient.votingsOfVoteCallerWithJwt(0, 10, jwt);

        // Then
        assertThat(statusOf(votingsResult), equalTo(FORBIDDEN));
    }

    private void setupVotingsAndToken() throws InterruptedException {
        CreateVotingRequest createVotingRequest = createValidVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.EMAILS);
        createVotingRequest.setAuthorizationEmailOptions(Arrays.asList("alice@mail.com", "doe@where.de", "some@one.com"));
        createVotingRequest.setSendInvites(true);
        createVotingRequest.setVisibility(CreateVotingRequest.Visibility.PRIVATE);
        createVotingRequest.setTitle("First voting");
        createVotingRequest.setVotesCap(3L);
        createVotingRequest.setOrganizer("William");

        votingIdOfFirst = voteCreationUtils.createVoting(createVotingRequest);
        Thread.sleep(3 * 100);

        JpaAuthToken authToken = Ebean.createQuery(JpaAuthToken.class)
                .where()
                .eq("voter.email", "alice@mail.com")
                .findOne();
        authTokenForAliceForFirstVoting = authToken.getToken().toString();

        createVotingRequest = createValidVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.EMAILS);
        createVotingRequest.setAuthorizationEmailOptions(Arrays.asList("alice@mail.com", "doe@where.de", "some@one.com"));
        createVotingRequest.setSendInvites(true);
        createVotingRequest.setOrganizer("William");
        createVotingRequest.setTitle("Second voting");
        createVotingRequest.setVisibility(CreateVotingRequest.Visibility.PRIVATE);
        createVotingRequest.setVotesCap(3L);

        votingIdOfSecond = voteCreationUtils.createVoting(createVotingRequest);
        Thread.sleep(3 * 100);
    }

    private void expireVotingBy40Days(String votingId) {
        // Expire the voting
        Long votingIdDecoded = Base62Conversions.decode(votingId);
        JpaVoting voting = Ebean.find(JpaVoting.class, votingIdDecoded);
        voting.setEndDate(Instant.now().minus(Duration.ofDays(40)));
        Ebean.save(voting);
    }

    private void waitForTokenCleanUp() throws InterruptedException {
        Thread.sleep(200);
    }
}
