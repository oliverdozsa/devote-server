package components.controllers.voting;

import play.libs.Json;
import requests.voting.CreateVotingRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;

public class VotingRequestMaker {
    public static CreateVotingRequest createValidVotingRequest() {
        return createValidVotingRequestEndingAt(Instant.now().plus(Duration.ofDays(1)));
    }

    public static CreateVotingRequest createValidVotingRequestEndingAt(Instant withEndDate) {
        InputStream sampleVotingIS = VotingControllerTest.class
                .getClassLoader().getResourceAsStream("voting-request-base.json");

        CreateVotingRequest votingRequest;
        try {
            votingRequest = Json.mapper().readValue(sampleVotingIS, CreateVotingRequest.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        votingRequest.setNetwork("mockblockchain");

        Instant tomorrow = Instant.now().plus(Duration.ofDays(1));
        votingRequest.setEncryptedUntil(tomorrow);

        Instant startDate = Instant.now();
        votingRequest.setStartDate(startDate);
        votingRequest.setEndDate(withEndDate);

        return votingRequest;
    }
}
