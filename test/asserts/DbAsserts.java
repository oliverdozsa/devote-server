package asserts;

import data.entities.Authorization;
import data.entities.JpaChannelAccountProgress;
import data.entities.JpaVoting;
import data.entities.JpaVotingAuthorizationEmail;
import data.entities.JpaVotingChannelAccount;
import data.entities.JpaVotingIssuerAccount;
import data.entities.JpaVotingPoll;
import data.entities.JpaVotingPollOption;
import io.ebean.Ebean;
import requests.CreatePollOptionRequest;
import requests.CreatePollRequest;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

public class DbAsserts {
    public static void assertChannelProgressCompletedFor(Long votingId) {
        int totalChannelAccounts = 0;

        List<JpaChannelAccountProgress> channelProgresses = channelProgressesOf(votingId);
        for(JpaChannelAccountProgress p: channelProgresses) {
            assertThat("accounts left to create", p.getNumOfAccountsLeftToCreate(), equalTo(0L));
            totalChannelAccounts += p.getNumOfAccountsToCreate();
        }

        assertThat("total accounts created", channelAccountsOf(votingId), hasSize(totalChannelAccounts));
    }

    public static void assertVoteTokensAreSavedInDb(Long votingId) {
        List<String> voteTokens = voteTokensOf(votingId);
        assertThat(voteTokens, notNullValue());
        assertThat(voteTokens, hasSize(greaterThan(0)));
    }

    public static void assertVotingEncryptionSavedInDb(Long votingId) {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);

        assertThat(voting.getEncryptionKey(), notNullValue());
        assertThat(voting.getEncryptionKey().length(), greaterThan(0));

        boolean isEncryptedUntilInFuture = voting.getEncryptedUntil().isAfter(Instant.now());
        assertTrue("Encrypted until is not in the future!", isEncryptedUntilInFuture);
    }

    public static void assertVotingStartEndDateSavedInDb(Long votingId) {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);

        assertThat(voting.getStartDate(), notNullValue());
        assertThat(voting.getEndDate(), notNullValue());

        boolean isEndDateAfterStartDate = voting.getEndDate().isAfter(voting.getStartDate());
        assertTrue("Voting end date is not after start date!", isEndDateAfterStartDate);
    }

    public static void assertAuthorizationEmailsSavedInDb(Long votingId, String... emails) {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);

        assertThat(voting.getAuthorization(), equalTo(Authorization.EMAILS));
        assertThat(voting.getAuthOptionsEmails(), hasSize(emails.length));

        List<String> storedEmailAddresses = toEmailsList(voting.getAuthOptionsEmails());
        assertThat(storedEmailAddresses, containsInAnyOrder(emails));
    }

    public static void assertPollSavedInDb(Long votingId, List<CreatePollRequest> expectedPolls) {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);

        List<JpaVotingPoll> savedPolls = voting.getPolls();
        assertThat(savedPolls, hasSize(expectedPolls.size()));

        expectedPolls.forEach(expectedPoll -> {
            JpaVotingPoll savedPoll = Ebean.find(JpaVotingPoll.class)
                    .where()
                    .eq("question", expectedPoll.getQuestion())
                    .findOne();

            assertThat(savedPoll, notNullValue());
            assertThat(savedPoll.getOptions(), hasSize(expectedPoll.getOptions().size()));
            assertSavedOptionAreTheSame(savedPoll.getOptions(), expectedPoll.getOptions());
        });
    }

    private static List<JpaChannelAccountProgress> channelProgressesOf(Long votingId) {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);
        return voting.getIssuerAccounts().stream()
                .map(JpaVotingIssuerAccount::getChannelAccountProgress)
                .collect(Collectors.toList());
    }

    private static List<JpaVotingChannelAccount> channelAccountsOf(Long votingId) {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);
        return voting.getChannelAccounts();
    }

    private static List<String> voteTokensOf(Long votingId) {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);
        return voting.getIssuerAccounts().stream()
                .map(JpaVotingIssuerAccount::getAssetCode)
                .collect(Collectors.toList());
    }

    private static List<String> toEmailsList(List<JpaVotingAuthorizationEmail> votingAuthorizationEmails) {
        return votingAuthorizationEmails.stream()
                .map(v -> v.getEmailAddress())
                .collect(Collectors.toList());
    }

    private static void assertSavedOptionAreTheSame(List<JpaVotingPollOption> savedOptions, List<CreatePollOptionRequest> expectedOptions) {
        assertSavedOptionNamesAreTheSame(savedOptions, expectedOptions);
        assertSavedOptionCodesAreTheSame(savedOptions, expectedOptions);
    }

    private static void assertSavedOptionNamesAreTheSame(List<JpaVotingPollOption> savedOptions, List<CreatePollOptionRequest> expectedOptions) {
        List<String> savedOptionNames = savedOptions.stream()
                .map(o -> o.getName())
                .collect(Collectors.toList());
        List<String> expectedOptionNames = expectedOptions.stream()
                .map(o -> o.getName())
                .collect(Collectors.toList());

        assertThat(savedOptionNames, hasSize(expectedOptionNames.size()));
        assertThat(savedOptionNames, containsInAnyOrder(expectedOptionNames.toArray()));
    }

    private static void assertSavedOptionCodesAreTheSame(List<JpaVotingPollOption> savedOptions, List<CreatePollOptionRequest> expectedOptions) {
        List<Integer> savedOptionCodes = savedOptions.stream()
                .map(o -> o.getCode())
                .collect(Collectors.toList());
        List<Integer> expectedOptionCodes = expectedOptions.stream()
                .map(o -> o.getCode())
                .collect(Collectors.toList());

        assertThat(savedOptionCodes, hasSize(expectedOptionCodes.size()));
        assertThat(savedOptionCodes, containsInAnyOrder(expectedOptionCodes.toArray()));
    }
}

