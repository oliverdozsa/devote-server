package controllers;

import asserts.IpfsAsserts;
import clients.VotingTestClient;
import ipfs.api.IpfsApi;
import ipfs.api.imp.MockIpfsApi;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import requests.CreateVotingRequest;
import rules.RuleChainForTests;
import services.Base62Conversions;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import static asserts.BlockchainAsserts.*;
import static asserts.DbAsserts.*;
import static controllers.VotingRequestMaker.createValidVotingRequest;
import static extractors.GenericDataFromResult.statusOf;
import static extractors.VotingResponseFromResult.idOf;
import static extractors.VotingResponseFromResult.networkOf;
import static matchers.ResultHasHeader.hasLocationHeader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static play.inject.Bindings.bind;
import static play.mvc.Http.HeaderNames.LOCATION;
import static play.mvc.Http.Status.*;

public class VotingControllerTest {
    private final RuleChainForTests ruleChainForTests;

    @Rule
    public RuleChain chain;

    private VotingTestClient client;
    private IpfsAsserts ipfsAsserts;

    public VotingControllerTest() {
        GuiceApplicationBuilder applicationBuilder = new GuiceApplicationBuilder()
                .overrides(bind(IpfsApi.class).to(MockIpfsApi.class));

        ruleChainForTests = new RuleChainForTests(applicationBuilder);
        chain = ruleChainForTests.getRuleChain();
    }

    @Before
    public void setup() {
        client = new VotingTestClient(ruleChainForTests.getApplication());
        ipfsAsserts = new IpfsAsserts(ruleChainForTests.getApplication());
    }

    @Test
    public void testCreate() throws InterruptedException, IOException {
        // Given
        CreateVotingRequest createVotingRequest = createValidVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.EMAILS);
        createVotingRequest.setAuthorizationEmailOptions(Arrays.asList("john@mail.com", "doe@where.de", "some@one.com"));

        // When
        Result result = client.createVoting(createVotingRequest);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String locationUrl = result.headers().get(LOCATION);
        Result getByLocationResult = client.byLocation(locationUrl);

        assertThat(statusOf(getByLocationResult), equalTo(OK));

        Long votingId = idOf(getByLocationResult);
        assertThat(votingId, greaterThan(0L));
        assertThat(networkOf(getByLocationResult), equalTo("mockblockchain"));
        assertIssuerAccountsCreatedOnBlockchain(votingId);
        assertDistributionAndBallotAccountsCreatedOnBlockchain(votingId);
        assertVoteTokensAreSavedInDb(votingId);
        assertVotingEncryptionSavedInDb(votingId);
        assertVotingStartEndDateSavedInDb(votingId);
        assertAuthorizationEmailsSavedInDb(votingId, "john@mail.com", "doe@where.de", "some@one.com");
        assertPollSavedInDb(votingId, createVotingRequest.getPolls());
        ipfsAsserts.assertVotingSavedToIpfs(votingId);

        Thread.sleep(30 * 1000);
        assertChannelAccountsCreatedOnBlockchain(votingId);
        assertChannelProgressCompletedFor(votingId);
    }

    @Test
    public void testBadRequestReturnedWhenCreatingVoteWithInvalidRequest() {
        // Given
        CreateVotingRequest createVotingRequest = createValidVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.EMAILS);
        createVotingRequest.setAuthorizationEmailOptions(Arrays.asList("john@mail.com", "doe@where.de", "some@one.com"));

        createVotingRequest.setStartDate(Instant.now());
        createVotingRequest.setEndDate(Instant.now().minus(Duration.ofDays(1)));

        // When
        Result result = client.createVoting(createVotingRequest);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    public void testWithInvalidOptions() {
        // Given
        CreateVotingRequest createVotingRequest = createValidVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.EMAILS);
        createVotingRequest.setAuthorizationEmailOptions(Arrays.asList("john@mail.com", "doe@where.de", "some@one.com"));

        createVotingRequest.getPolls().get(0).getOptions().get(1).setCode(1);

        // When
        Result result = client.createVoting(createVotingRequest);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }

    @Test
    public void testSingle_NonExistingId() {
        // Given
        String nonExistingVotingId = Base62Conversions.encode(42L);

        // When
        Result result = client.single(nonExistingVotingId);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    public void testSingle_IdNotEncodedInBase62() {
        // Given
        String wrongEncoding = "+=";

        // When
        Result result = client.single(wrongEncoding);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
    }
}
