package controllers;

import clients.VotingTestClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.mvc.Result;
import requests.CreateVotingRequest;
import rules.RuleChainForTests;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import static asserts.BlockchainAsserts.*;
import static asserts.DbAsserts.*;
import static asserts.IpfsAsserts.assertVotingSavedToIpfs;
import static controllers.VotingRequestMaker.createValidVotingRequest;
import static extractors.GenericDataFromResult.statusOf;
import static extractors.VotingResponseFromResult.idOf;
import static extractors.VotingResponseFromResult.networkOf;
import static matchers.ResultHasHeader.hasLocationHeader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static play.mvc.Http.HeaderNames.LOCATION;
import static play.mvc.Http.Status.*;

public class VotingControllerTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private VotingTestClient client;

    @Before
    public void setup() {
        client = new VotingTestClient(ruleChainForTests.getApplication());
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
        assertVotingSavedToIpfs(votingId);

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

    // TODO: Test single with non existing ID -> not found expected
}
