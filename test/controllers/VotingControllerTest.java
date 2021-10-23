package controllers;

import clients.VotingTestClient;
import com.typesafe.config.Config;
import dto.CreateVotingRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import play.libs.Json;
import play.mvc.Result;
import rules.RuleChainForTests;

import java.io.IOException;
import java.io.InputStream;

import static asserts.BlockchainAsserts.assertIssuerAccountsCreatedOnBlockchain;
import static extractors.GenericDataFromResult.statusOf;
import static extractors.VotingResponseFromResult.idOf;
import static extractors.VotingResponseFromResult.networkOf;
import static matchers.ResultHasLocationHeader.hasLocationHeader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static play.mvc.Http.HeaderNames.LOCATION;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.OK;

public class VotingControllerTest {
    private final RuleChainForTests ruleChainForTests = new RuleChainForTests();

    @Rule
    public RuleChain chain = ruleChainForTests.getRuleChain();

    private VotingTestClient client;
    private Config config;

    @Before
    public void setup() {
        client = new VotingTestClient(ruleChainForTests.getApplication());
        config = ruleChainForTests.getApplication().config();
    }

    @Test
    public void testCreate() throws IOException, InterruptedException {
        // Given
        CreateVotingRequest createVotingRequest = createValidVotingRequest();

        // When
        Result result = client.createVoting(createVotingRequest);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String locationUrl = result.headers().get(LOCATION);
        Result getByLocationResult = client.byLocation(locationUrl);

        assertThat(statusOf(getByLocationResult), equalTo(OK));
        assertThat(idOf(getByLocationResult), greaterThan(0L));
        assertThat(networkOf(getByLocationResult), equalTo("mockblockchain"));
        assertIssuerAccountsCreatedOnBlockchain(idOf(getByLocationResult));

        // TODO: Wait for channel accounts creation then assert.
        //

    }

    private static CreateVotingRequest createValidVotingRequest() throws IOException {
        InputStream sampleVotingIS = VotingControllerTest.class
                .getClassLoader().getResourceAsStream("voting-request-base.json");
        CreateVotingRequest votingRequest = Json.mapper().readValue(sampleVotingIS, CreateVotingRequest.class);
        votingRequest.setNetwork("mockblockchain");

        return votingRequest;
    }
}
