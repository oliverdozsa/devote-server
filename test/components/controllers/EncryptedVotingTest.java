package components.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import components.clients.CommissionTestClient;
import components.clients.VotingTestClient;
import components.extractors.GenericDataFromResult;
import crypto.AesCtrCrypto;
import data.entities.JpaVoting;
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
import java.util.Base64;

import static components.extractors.CommissionResponseFromResult.encryptedOptionCodeOf;
import static components.extractors.GenericDataFromResult.statusOf;
import static components.extractors.VotingResponseFromResult.decryptionKeyOf;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertTrue;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.*;

public class EncryptedVotingTest {
    @Rule
    public RuleChain chain;

    private final RuleChainForTests ruleChainForTests;

    private CommissionTestClient testClient;
    private VotingTestClient votingTestClient;
    private VoteCreationUtils voteCreationUtils;

    public EncryptedVotingTest() {
        GuiceApplicationBuilder applicationBuilder = new GuiceApplicationBuilder()
                .overrides(bind(IpfsApi.class).to(MockIpfsApi.class))
                .overrides(bind(IPFS.class).toProvider(MockIpfsProvider.class))
                .overrides((bind(JwtVerification.class).to(JwtVerificationForTests.class)));

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
    public void testEncryptChoiceForVoting() throws InterruptedException {
        // Given
        String votingId = voteCreationUtils.createValidVoting();

        // When
        String aChoice = "0142";
        Result anEncryptedOptionResult = testClient.encryptChoice(votingId, aChoice);
        Result anotherEncryptedOptionResult = testClient.encryptChoice(votingId, aChoice);

        // Then
        assertThat(statusOf(anEncryptedOptionResult), equalTo(OK));
        assertThat(statusOf(anotherEncryptedOptionResult), equalTo(OK));

        String firstEncryptedOptionCode = encryptedOptionCodeOf(anEncryptedOptionResult);
        String secondEncryptedOptionCode = encryptedOptionCodeOf(anotherEncryptedOptionResult);

        assertThat(firstEncryptedOptionCode, notNullValue());
        assertThat(firstEncryptedOptionCode.length(), greaterThan(0));
        assertThat(secondEncryptedOptionCode, notNullValue());
        assertThat(secondEncryptedOptionCode.length(), greaterThan(0));
        assertThat(firstEncryptedOptionCode, not(equalTo(secondEncryptedOptionCode)));
    }

    @Test
    public void testEncryptChoiceForVoting_InvalidChoice() throws InterruptedException {
        // Given
        String votingId = voteCreationUtils.createValidVoting();

        // When
        String anInvalidChoice = "123";
        Result anEncryptedOptionResult = testClient.encryptChoice(votingId, anInvalidChoice);

        // Then
        assertThat(statusOf(anEncryptedOptionResult), equalTo(BAD_REQUEST));

    }

    @Test
    public void testEncryptChoiceForVoting_VotingDoesNotExist() throws InterruptedException {
        // Given
        voteCreationUtils.createValidVoting();

        // When
        String aChoice = "1234";
        Result anEncryptedOptionResult = testClient.encryptChoice(Base62Conversions.encode(42L), aChoice);

        // Then
        assertThat(statusOf(anEncryptedOptionResult), equalTo(NOT_FOUND));
    }

    @Test
    public void testEncryptChoiceForVoting_VotingIsNotEncrypted() throws InterruptedException {
        // Given
        String votingId = voteCreationUtils.createValidNotEncryptedVoting();

        // When
        String aChoice = "1234";
        Result anEncryptedOptionResult = testClient.encryptChoice(votingId, aChoice);

        // Then
        assertThat(statusOf(anEncryptedOptionResult), equalTo(BAD_REQUEST));
    }

    @Test
    public void testEncryptChoiceEncryptedUntilExpires() throws InterruptedException {
        // Given
        String votingId = voteCreationUtils.createValidVotingEncryptedUntil(Instant.now().plusSeconds(9));

        // When
        String aChoice = "1234";
        Result anEncryptedOptionResult = testClient.encryptChoice(votingId, aChoice);
        Result anotherEncryptedOptionResult = testClient.encryptChoice(votingId, aChoice);

        // Wait for expiration of encrypted until.
        Thread.sleep(11 * 1000);

        Result votingResult = votingTestClient.single(votingId);

        // Then
        assertThat(statusOf(anEncryptedOptionResult), equalTo(OK));
        assertThat(statusOf(anotherEncryptedOptionResult), equalTo(OK));
        assertThat(statusOf(votingResult), equalTo(OK));

        String firstEncryptedOptionCode = encryptedOptionCodeOf(anEncryptedOptionResult);
        String secondEncryptedOptionCode = encryptedOptionCodeOf(anotherEncryptedOptionResult);

        assertThat(firstEncryptedOptionCode, notNullValue());
        assertThat(firstEncryptedOptionCode.length(), greaterThan(0));
        assertThat(secondEncryptedOptionCode, notNullValue());
        assertThat(secondEncryptedOptionCode.length(), greaterThan(0));
        assertThat(firstEncryptedOptionCode, not(equalTo(secondEncryptedOptionCode)));

        String decryptionKey = decryptionKeyOf(votingResult);
        assertThat(decryptionKey, notNullValue());
        assertThat(decryptionKey.length(), greaterThan(0));

        byte[] decryptionKeyBytes = Base64.getDecoder().decode(decryptionKey);
        byte[] firstOptionCodeCipherBytes = Base64.getDecoder().decode(firstEncryptedOptionCode);
        byte[] secondOptionCodeCipherBytes = Base64.getDecoder().decode(secondEncryptedOptionCode);

        byte[] firstDecryptedBytes = AesCtrCrypto.decrypt(decryptionKeyBytes, firstOptionCodeCipherBytes);
        byte[] secondDecryptedBytes = AesCtrCrypto.decrypt(decryptionKeyBytes, secondOptionCodeCipherBytes);

        String firstDecryptedString = new String(firstDecryptedBytes);
        String secondDecryptedString = new String(secondDecryptedBytes);

        assertThat(firstDecryptedString, equalTo(aChoice));
        assertThat(secondDecryptedString, equalTo(aChoice));
    }

    @Test
    public void testEncryptedUntilHasNotExpiredYet() {
        // Given
        String votingId = voteCreationUtils.createValidVotingEncryptedUntil(Instant.now().plusSeconds(9));

        // When
        Result votingResult = votingTestClient.single(votingId);

        // Then
        JsonNode votingResultJson = GenericDataFromResult.jsonOf(votingResult);
        assertTrue(votingResultJson.get("decryptionKey").isNull());
    }

    @Test
    public void testEncryptChoiceForVoting_VotingEnded() throws InterruptedException {
        // Given
        String votingId = voteCreationUtils.createValidVoting();

        endVoting(votingId);

        // When
        String aChoice = "0142";
        Result anEncryptedOptionResult = testClient.encryptChoice(votingId, aChoice);

        // Then
        assertThat(statusOf(anEncryptedOptionResult), equalTo(FORBIDDEN));
    }

    @Test
    public void testEncryptChoiceForVoting_VotingNotStartedYet() throws InterruptedException {
        // Given
        String votingId = voteCreationUtils.createValidVoting();

        startVotingInFuture(votingId);

        // When
        String aChoice = "0142";
        Result anEncryptedOptionResult = testClient.encryptChoice(votingId, aChoice);

        // Then
        assertThat(statusOf(anEncryptedOptionResult), equalTo(FORBIDDEN));
    }

    private void endVoting(String votingIdEncoded) {
        long votingId = Base62Conversions.decode(votingIdEncoded);
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);
        voting.setEndDate(Instant.now().minus(Duration.ofSeconds(15)));
        Ebean.update(voting);
    }

    private void startVotingInFuture(String votingIdEncoded) {
        long votingId = Base62Conversions.decode(votingIdEncoded);
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);
        voting.setStartDate(Instant.now().plus(Duration.ofSeconds(15)));
        Ebean.update(voting);
    }
}
