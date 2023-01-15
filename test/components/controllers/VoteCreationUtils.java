package components.controllers;

import components.clients.CommissionTestClient;
import components.clients.VotingTestClient;
import play.libs.Json;
import play.mvc.Result;
import requests.CommissionInitRequest;
import requests.CreateVotingRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import static components.controllers.VotingRequestMaker.createValidVotingRequest;
import static components.extractors.CommissionResponseFromResult.publicKeyOf;
import static components.extractors.GenericDataFromResult.statusOf;
import static matchers.ResultHasHeader.hasLocationHeader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static play.mvc.Http.HeaderNames.LOCATION;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.OK;

public class VoteCreationUtils {
    public final CommissionTestClient testClient;
    public final VotingTestClient votingTestClient;

    public VoteCreationUtils(CommissionTestClient testClient, VotingTestClient votingTestClient) {
        this.testClient = testClient;
        this.votingTestClient = votingTestClient;
    }

    public String createValidVotingWithWaitingForFullInit() throws InterruptedException {
        String votingId = createValidVoting();
        Thread.sleep(3 * 100);
        return votingId;
    }

    public String createValidVoting() {
        CreateVotingRequest createVotingRequest = createValidVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.EMAILS);
        createVotingRequest.setAuthorizationEmailOptions(Arrays.asList("alice@mail.com", "doe@where.de", "some@one.com"));

        return createVoting(createVotingRequest);
    }

    public String createValidVotingEndingInSecondsFromNow(int seconds) {
        CreateVotingRequest createVotingRequest = VotingRequestMaker.createValidVotingRequestEndingAt(Instant.now().plus(Duration.ofSeconds(seconds)));
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.EMAILS);
        createVotingRequest.setAuthorizationEmailOptions(Arrays.asList("alice@mail.com", "doe@where.de", "some@one.com"));

        return createVoting(createVotingRequest);
    }

    public String createValidVotingEncryptedUntil(Instant encryptedUntil) {
        CreateVotingRequest createVotingRequest = createValidVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.EMAILS);
        createVotingRequest.setAuthorizationEmailOptions(Arrays.asList("john@mail.com", "doe@where.de", "some@one.com"));
        createVotingRequest.setEncryptedUntil(encryptedUntil);

        return createVoting(createVotingRequest);
    }

    public String createValidNotEncryptedVoting() {
        CreateVotingRequest createVotingRequest = createValidVotingRequest();
        createVotingRequest.setAuthorization(CreateVotingRequest.Authorization.EMAILS);
        createVotingRequest.setAuthorizationEmailOptions(Arrays.asList("john@mail.com", "doe@where.de", "some@one.com"));
        createVotingRequest.setEncryptedUntil(null);

        return createVoting(createVotingRequest);
    }

    public String createVoting(CreateVotingRequest createVotingRequest) {
        Result result = votingTestClient.createVoting(createVotingRequest, "Alice", "alice@mail.com");
        assertThat(statusOf(result), equalTo(CREATED));
        assertThat(result, hasLocationHeader());

        String locationUrl = result.headers().get(LOCATION);
        String[] locationUrlParts = locationUrl.split("/");
        String votingId = locationUrlParts[locationUrlParts.length - 1];

        return votingId;
    }

    public InitData initVotingFor(String userId) throws InterruptedException {
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

        return initData;
    }

    public String createMessage(String votingId, String voterPublicAccountId) {
        return votingId + "|" + voterPublicAccountId;
    }

    public static class InitData {
        public String votingId;
        public String publicKey;
    }
}
