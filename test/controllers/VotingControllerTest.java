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

import static extractors.GenericDataFromResult.statusOf;
import static matchers.ResultHasLocationHeader.hasLocationHeader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static play.mvc.Http.Status.CREATED;

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
    public void testCreate() throws IOException {
        // Given
        // When
        CreateVotingRequest createVotingRequest = createValidVotingRequest();
        Result result = client.createVoting(createVotingRequest);

        // Then
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        // TODO
    }

    private static CreateVotingRequest createValidVotingRequest() throws IOException {
        InputStream sampleVotingIS = VotingControllerTest.class
                .getClassLoader().getResourceAsStream("voting-request-base.json");
        CreateVotingRequest votingRequest = Json.mapper().readValue(sampleVotingIS, CreateVotingRequest.class);
        votingRequest.setNetwork("mockblockchain");

        return votingRequest;
    }

}
