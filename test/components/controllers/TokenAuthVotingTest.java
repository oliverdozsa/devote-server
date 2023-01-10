package components.controllers;

import components.clients.CommissionTestClient;
import components.clients.TokenAuthTestClient;
import components.clients.VotingTestClient;
import data.entities.JpaAuthToken;
import io.ebean.Ebean;
import io.ipfs.api.IPFS;
import ipfs.api.IpfsApi;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import requests.CreateVotingRequest;
import rules.RuleChainForTests;
import security.jwtverification.JwtVerification;
import security.jwtverification.JwtVerificationForTests;
import units.ipfs.api.imp.MockIpfsApi;
import units.ipfs.api.imp.MockIpfsProvider;

import java.util.Arrays;

import static components.controllers.VotingRequestMaker.createValidVotingRequest;
import static components.extractors.GenericDataFromResult.statusOf;
import static components.extractors.TokenAuthResponseFromResult.jwtOf;
import static components.extractors.VotingResponseFromResult.titleOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.FORBIDDEN;
import static play.mvc.Http.Status.OK;

public class TokenAuthVotingTest {
    @Rule
    public RuleChain chain;

    private final RuleChainForTests ruleChainForTests;

    private CommissionTestClient commissionTestClient;
    private VotingTestClient votingTestClient;
    private VoteCreationUtils voteCreationUtils;
    private TokenAuthTestClient tokenAuthTestClient;

    private String authTokenForAliceForFirstVoting;
    private String votingIdOfFirst;
    private String votingIdOfSecond;

    public TokenAuthVotingTest() {
        GuiceApplicationBuilder applicationBuilder = new GuiceApplicationBuilder()
                .overrides(bind(IpfsApi.class).to(MockIpfsApi.class))
                .overrides(bind(IPFS.class).toProvider(MockIpfsProvider.class))
                .overrides((bind(JwtVerification.class).to(JwtVerificationForTests.class)));

        ruleChainForTests = new RuleChainForTests(applicationBuilder);
        chain = ruleChainForTests.getRuleChain();
    }

    @Before
    public void setup() throws InterruptedException {
        tokenAuthTestClient = new TokenAuthTestClient(ruleChainForTests.getApplication());
        commissionTestClient = new CommissionTestClient(ruleChainForTests.getApplication());
        votingTestClient = new VotingTestClient(ruleChainForTests.getApplication());
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
    public void testAccessingSingleVote_TokeIsForOtherVoting() {
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
    public void testAccessingVotesViaToken() {
        // Given
        // When
        // Then

        // TODO
        fail();
    }

    @Test
    public void castingVoteViaToken() {
        // Given
        // When
        // Then

        // TODO
        fail();
    }

    @Test
    public void testAccessingSingleVoteViaAuthAndTokenExistsForEmail() {
        // Given
        // When
        // Then

        // TODO
        fail();
    }

    @Test
    public void testAccessingVotesViaAuthAndTokenExistsForEmail() {
        // Given
        // When
        // Then

        // TODO
        fail();
    }

    @Test
    public void testCastingVoteInTokenBasedVotingThroughAuth() {
        // Given
        // When
        // Then

        // TODO
        fail();
    }

    @Test
    public void testAccessingVotesViaToken_TokenExpired() {
        // Given
        // When
        // Then

        // TODO
        fail();
    }

    @Test
    public void testAccessingSingleVoteViaToken_TokenExpired() {
        // Given
        // When
        // Then

        // TODO
        fail();
    }

    @Test
    public void testTokenNotFound() {
        // Given
        // When
        // Then

        // TODO
        fail();
    }

    private void setupVotingsAndToken() throws InterruptedException {
        CreateVotingRequest createVotingRequest = createValidVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.EMAILS);
        createVotingRequest.setAuthorizationEmailOptions(Arrays.asList("alice@mail.com", "doe@where.de", "some@one.com"));
        createVotingRequest.setSendInvites(true);
        createVotingRequest.setVisibility(CreateVotingRequest.Visibility.PRIVATE);
        createVotingRequest.setTitle("First voting");

        votingIdOfFirst = voteCreationUtils.createVoting(createVotingRequest);

        JpaAuthToken authToken = Ebean.createQuery(JpaAuthToken.class)
                .where()
                .eq("voter.email", "alice@mail.com")
                .findOne();
        authTokenForAliceForFirstVoting = authToken.getToken().toString();

        createVotingRequest = createValidVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.EMAILS);
        createVotingRequest.setAuthorizationEmailOptions(Arrays.asList("alice@mail.com", "doe@where.de", "some@one.com"));
        createVotingRequest.setSendInvites(true);
        createVotingRequest.setTitle("Second voting");

        votingIdOfSecond = voteCreationUtils.createVoting(createVotingRequest);
        Thread.sleep(3 * 1000);
    }
}
