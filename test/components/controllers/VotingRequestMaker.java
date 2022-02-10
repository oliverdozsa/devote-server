package components.controllers;

import play.libs.Json;
import requests.CreateVotingRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;

public class VotingRequestMaker {
    public static CreateVotingRequest createValidVotingRequest() {
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
        Instant endDate = Instant.now().plus(Duration.ofDays(1));
        votingRequest.setStartDate(startDate);
        votingRequest.setEndDate(endDate);

        return votingRequest;
    }
}
