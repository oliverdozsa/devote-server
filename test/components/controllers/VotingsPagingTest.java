package components.controllers;

import com.github.database.rider.core.api.dataset.DataSet;
import components.clients.VotingsPagingTestClient;
import data.entities.*;
import io.ebean.Ebean;
import io.ipfs.api.IPFS;
import ipfs.api.IpfsApi;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import rules.RuleChainForTests;
import security.jwtverification.JwtVerification;
import security.jwtverification.JwtVerificationForTests;
import services.Base62Conversions;
import units.ipfs.api.imp.MockIpfsApi;
import units.ipfs.api.imp.MockIpfsProvider;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import static components.extractors.GenericDataFromResult.statusOf;
import static components.extractors.VotingPagingItemResponsesFromResult.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.FORBIDDEN;
import static play.mvc.Http.Status.OK;

public class VotingsPagingTest {
    private final RuleChainForTests ruleChainForTests;

    @Rule
    public RuleChain chain;

    private VotingsPagingTestClient client;

    public VotingsPagingTest() {
        GuiceApplicationBuilder applicationBuilder = new GuiceApplicationBuilder()
                .overrides(bind(IpfsApi.class).to(MockIpfsApi.class))
                .overrides(bind(IPFS.class).toProvider(MockIpfsProvider.class))
                .overrides((bind(JwtVerification.class).qualifiedWith("auth0").to(JwtVerificationForTests.class)));

        ruleChainForTests = new RuleChainForTests(applicationBuilder);
        chain = ruleChainForTests.getRuleChain();
    }

    @Before
    public void setup() {
        client = new VotingsPagingTestClient(ruleChainForTests.getApplication());
        seedPublicVotingTestData();
    }

    @Test
    @DataSet(value = "datasets/yml/votings-paging.yml", disableConstraints = true, cleanBefore = true)
    public void testGetPublicVotingsPaging() {
        // Given
        // When
        Result result = client.publicVotings(0, 11);

        // Then
        assertThat(statusOf(result), equalTo(OK));

        assertThat(votingIdsOf(result).size(), equalTo(11));
        assertThat(votingIdsOf(result), everyItem(notNullValue(String.class)));

        assertThat(votingTitlesOf(result).size(), equalTo(11));
        assertThat(votingTitlesOf(result), everyItem(notNullValue(String.class)));

        assertThat(endDatesOf(result).size(), equalTo(11));
        assertThat(endDatesOf(result), everyItem(notNullValue(String.class)));


    }

    @Test
    @DataSet(value = "datasets/yml/votings-paging.yml", disableConstraints = true, cleanBefore = true)
    public void testGetVotingsOfVoterPaging() {
        // Given
        // When
        Result result = client.votingsOfVoter(0, 11, "Alice");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(votingIdsOf(result).size(), equalTo(2));
        assertThat(votingIdsOf(result), everyItem(notNullValue(String.class)));

        assertThat(votingTitlesOf(result).size(), equalTo(2));
        assertThat(votingTitlesOf(result), everyItem(notNullValue(String.class)));

        assertThat(endDatesOf(result).size(), equalTo(2));
        assertThat(endDatesOf(result), everyItem(notNullValue(String.class)));
    }

    @Test
    @DataSet(value = "datasets/yml/votings-paging.yml", disableConstraints = true, cleanBefore = true)
    public void testGetVotingsOfVoterPaging_NoProperRole() {
        // Given
        // When
        Result result = client.votingsOfVoter(0, 11, "Alice", new String[]{});

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    @DataSet(value = "datasets/yml/votings-paging.yml", disableConstraints = true, cleanBefore = true)
    public void testGetVoterVotingsPaging_NotAParticipantInAnyVote() {
        // Given
        // When
        Result result = client.votingsOfVoter(0, 11, "Someone");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(votingIdsOf(result).size(), equalTo(0));
        assertThat(votingTitlesOf(result).size(), equalTo(0));
        assertThat(endDatesOf(result).size(), equalTo(0));
    }

    @Test
    @DataSet(value = "datasets/yml/votings-paging.yml", disableConstraints = true, cleanBefore = true)
    public void testVoteCallersVotePaging() {
        // Given
        // When
        Result result = client.votingsOfVoteCaller(0, 11, "Bob");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(votingIdsOf(result).size(), equalTo(2));
        assertThat(votingIdsOf(result), everyItem(notNullValue(String.class)));

        assertThat(votingTitlesOf(result).size(), equalTo(2));
        assertThat(votingTitlesOf(result), everyItem(notNullValue(String.class)));

        assertThat(endDatesOf(result).size(), equalTo(2));
        assertThat(endDatesOf(result), everyItem(notNullValue(String.class)));
    }

    @Test
    @DataSet(value = "datasets/yml/votings-paging.yml", disableConstraints = true, cleanBefore = true)
    public void testVoteCallersVotePaging_NoProperRole() {
        // Given
        // When
        Result result = client.votingsOfVoteCaller(0, 11, "Bob", new String[]{});

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    @DataSet(value = "datasets/yml/votings-paging.yml", disableConstraints = true, cleanBefore = true)
    public void testVoteCallersVotePaging_ParticipantButNotCreatedAnyVote() {
        // Given
        // When
        Result result = client.votingsOfVoteCaller(0, 11, "Eva");

        // Then
        assertThat(statusOf(result), equalTo(OK));
        assertThat(votingIdsOf(result).size(), equalTo(0));
        assertThat(votingTitlesOf(result).size(), equalTo(0));
        assertThat(endDatesOf(result).size(), equalTo(0));
    }

    @Test
    @DataSet(value = "datasets/yml/votings-paging.yml", disableConstraints = true, cleanBefore = true)
    public void testVoterVotingNotTriedToVote() {
        // Given
        // When
        JpaVoter jpaAliceVoter = Ebean.find(JpaVoter.class, 1L);
        JpaVoter jpaBobVoter = Ebean.find(JpaVoter.class, 2L);

        JpaVoting jpaVoting = new JpaVoting();
        jpaVoting.setId(142L);
        jpaVoting.setTitle("Vote 142");
        jpaVoting.setAuthorization(Authorization.EMAILS);
        jpaVoting.setVisibility(Visibility.PRIVATE);
        jpaVoting.setBallotType(BallotType.MULTI_POLL);
        jpaVoting.setCreatedBy("Bob");
        jpaVoting.setNetwork("mockblockchain");
        jpaVoting.setVotesCap(100L);
        jpaVoting.setCreatedAt(Instant.now().minus(Duration.ofDays(1)));
        jpaVoting.setStartDate(Instant.now().minus(Duration.ofDays(1)));
        jpaVoting.setEndDate(Instant.now().plus(Duration.ofDays(1)));
        jpaVoting.setVoters(Arrays.asList(jpaAliceVoter, jpaBobVoter));
        Ebean.save(jpaVoting);

        JpaCommissionSession initSessionAlice = new JpaCommissionSession();
        initSessionAlice.setVoter(jpaAliceVoter);
        initSessionAlice.setVoting(jpaVoting);
        Ebean.save(initSessionAlice);

        Result votesResult = client.votingsOfVoterFilteredByNotAlreadyTriedToVote(0, 10, "Bob");
        assertThat(statusOf(votesResult), equalTo(OK));

        assertThat(votingIdsDecodedOf(votesResult).size(), equalTo(3));
        assertThat(votingIdsDecodedOf(votesResult), containsInAnyOrder(1L, 2L, 142L));

        JpaCommissionSession initSessionBob = new JpaCommissionSession();
        initSessionBob.setVoter(jpaBobVoter);
        initSessionBob.setVoting(jpaVoting);
        Ebean.save(initSessionBob);

        votesResult = client.votingsOfVoterFilteredByNotAlreadyTriedToVote(0, 10, "Bob");
        assertThat(statusOf(votesResult), equalTo(OK));

        assertThat(votingIdsDecodedOf(votesResult).size(), equalTo(2));
        assertThat(votingIdsDecodedOf(votesResult), containsInAnyOrder(1L, 2L));
    }

    private void seedPublicVotingTestData() {
        for(int i = 42; i < 84; i++) {
            JpaVoting jpaVoting = new JpaVoting();
            jpaVoting.setId((long) i);
            jpaVoting.setTitle("Public Voting#" + i);
            jpaVoting.setVisibility(Visibility.PUBLIC);
            jpaVoting.setAuthorization(Authorization.EMAILS);
            jpaVoting.setCreatedBy("Walter");
            jpaVoting.setNetwork("mockblockchain");
            jpaVoting.setVotesCap(42L);
            jpaVoting.setCreatedAt(Instant.now().minus(Duration.ofDays(1)));
            jpaVoting.setStartDate(Instant.now().minus(Duration.ofDays(1)));
            jpaVoting.setEndDate(Instant.now().plus(Duration.ofDays(1)));
            jpaVoting.setBallotType(BallotType.MULTI_POLL);
            Ebean.save(jpaVoting);
        }
    }
}
