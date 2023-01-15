package components.controllers;

import asserts.DbAsserts;
import components.clients.CommissionTestClient;
import components.clients.VotingTestClient;
import data.entities.JpaVoting;
import io.ebean.Ebean;
import io.ipfs.api.IPFS;
import ipfs.api.IpfsApi;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.inject.guice.GuiceApplicationBuilder;
import rules.RuleChainForTests;
import security.jwtverification.JwtVerification;
import security.jwtverification.JwtVerificationForTests;
import services.Base62Conversions;
import units.ipfs.api.imp.MockIpfsApi;
import units.ipfs.api.imp.MockIpfsProvider;

import java.time.Duration;
import java.time.Instant;

import static play.inject.Bindings.bind;

public class RefundBalancesTest {
    @Rule
    public RuleChain chain;

    private final RuleChainForTests ruleChainForTests;

    private CommissionTestClient testClient;
    private VotingTestClient votingTestClient;
    private VoteCreationUtils voteCreationUtils;

    public RefundBalancesTest() {
        GuiceApplicationBuilder applicationBuilder = new GuiceApplicationBuilder()
                .overrides(bind(IpfsApi.class).to(MockIpfsApi.class))
                .overrides(bind(IPFS.class).toProvider(MockIpfsProvider.class))
                .overrides((bind(JwtVerification.class).qualifiedWith("auth0").to(JwtVerificationForTests.class)));

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
    public void testBalancesAreRefund() throws InterruptedException {
        // Given
        String votingId = voteCreationUtils.createValidVotingEndingInSecondsFromNow(10);
        String otherVotingId = voteCreationUtils.createValidVoting(); // Expires tomorrow

        Thread.sleep(300);

        Long votingIdDecoded = Base62Conversions.decode(votingId);
        DbAsserts.assertChannelAccountsAreMarkedAsNotRefunded(votingIdDecoded);
        DbAsserts.assertChannelGeneratorsAreMarkedAsNotRefunded(votingIdDecoded);
        DbAsserts.assertDistributionAccountIsNotMarkedAsRefunded(votingIdDecoded);

        // Expire voting
        JpaVoting voting = Ebean.find(JpaVoting.class, votingIdDecoded);
        voting.setEndDate(Instant.now().minus(Duration.ofSeconds(10)));
        Ebean.save(voting);

        // When
        // Also account for task trigger time.
        Thread.sleep(14 * 100);

        //Then
        DbAsserts.assertChannelAccountsAreMarkedAsRefunded(votingIdDecoded);
        DbAsserts.assertChannelGeneratorsAreMarkedAsRefunded(votingIdDecoded);
        DbAsserts.assertDistributionAccountIsMarkedAsRefunded(votingIdDecoded);
        DbAsserts.assertInternalFundingIsMarkedAsRefunded(votingIdDecoded);
    }
}
