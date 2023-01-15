package asserts;

import data.entities.Authorization;
import data.entities.JpaChannelAccountProgress;
import data.entities.JpaChannelGeneratorAccount;
import data.entities.JpaStoredTransaction;
import data.entities.JpaVoter;
import data.entities.JpaVoting;
import data.entities.JpaVotingChannelAccount;
import data.entities.JpaVotingPoll;
import data.entities.JpaVotingPollOption;
import io.ebean.Ebean;
import requests.CreatePollOptionRequest;
import requests.CreatePollRequest;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DbAsserts {
    public static void assertChannelProgressCompletedFor(Long votingId) {
        int totalChannelAccounts = 0;

        List<JpaChannelAccountProgress> channelProgresses = channelProgressesOf(votingId);
        for (JpaChannelAccountProgress p : channelProgresses) {
            assertThat("accounts left to create", p.getNumOfAccountsLeftToCreate(), equalTo(0L));
            totalChannelAccounts += p.getNumOfAccountsToCreate();
        }

        assertThat("total accounts created", channelAccountsOf(votingId), hasSize(totalChannelAccounts));
    }

    public static void assertVoteTokenIsSavedInDb(Long votingId) {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);

        assertThat(voting.getAssetCode(), notNullValue());
        assertThat(voting.getAssetCode().length(), greaterThan(0));
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
        assertThat(voting.getVoters(), hasSize(emails.length));

        List<String> storedEmailAddresses = voting.getVoters().stream()
                .map(JpaVoter::getEmail)
                .collect(Collectors.toList());
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

    public static void assertThatTransactionIsStoredFor(String signature) {
        Optional<JpaStoredTransaction> storedTransactionOptional = Ebean.createQuery(JpaStoredTransaction.class)
                .where()
                .eq("signature", signature)
                .findOneOrEmpty();
        assertTrue("Not found stored transaction for signature: " + signature, storedTransactionOptional.isPresent());
    }

    public static void assertChannelGeneratorsCreatedOnTestMockBlockchainNetwork() {
        List<JpaChannelGeneratorAccount> channelGenerators = Ebean.createQuery(JpaChannelGeneratorAccount.class)
                .findList();
        channelGenerators.forEach(cg -> assertThat(cg.getAccountPublic(), startsWith("test-net")));
    }

    public static void assertVotersAreUnique() {
        List<String> voterEmails = Ebean.createQuery(JpaVoter.class)
                .findList()
                .stream().map(JpaVoter::getEmail)
                .collect(Collectors.toList());

        Set<String> uniqueVoterEmails = new HashSet<>(voterEmails);

        assertThat(voterEmails.size(), equalTo(uniqueVoterEmails.size()));
    }

    public static void assertChannelGeneratorsAreMarkedAsRefunded(Long votingId) {
        List<JpaChannelGeneratorAccount> channelGenerators = Ebean.find(JpaChannelGeneratorAccount.class)
                .where()
                .eq("voting.id", votingId)
                .findList();

        List<Boolean> channelGeneratorsRefunded = channelGenerators.stream()
                .map(JpaChannelGeneratorAccount::isRefunded)
                .collect(Collectors.toList());

        assertThat(channelGeneratorsRefunded, everyItem(is(true)));
    }

    public static void assertChannelAccountsAreMarkedAsRefunded(Long votingId) {
        List<JpaVotingChannelAccount> channelAccounts = Ebean.find(JpaVotingChannelAccount.class)
                .where()
                .eq("voting.id", votingId)
                .findList();

        List<Boolean> channelAccountsRefunded = channelAccounts.stream()
                .map(JpaVotingChannelAccount::isRefunded)
                .collect(Collectors.toList());

        assertThat(channelAccountsRefunded, everyItem(is(true)));
    }

    public static void assertDistributionAccountIsMarkedAsRefunded(Long votingId) {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);
        assertThat(voting.isDistributionRefunded(), is(true));
    }

    public static void assertInternalFundingIsMarkedAsRefunded(Long votingId) {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);
        assertThat(voting.isInternalFundingRefunded(), is(true));
    }

    public static void assertChannelGeneratorsAreMarkedAsNotRefunded(Long votingId) {
        List<JpaChannelGeneratorAccount> channelGenerators = Ebean.find(JpaChannelGeneratorAccount.class)
                .where()
                .eq("voting.id", votingId)
                .findList();

        List<Boolean> channelGeneratorsRefunded = channelGenerators.stream()
                .map(JpaChannelGeneratorAccount::isRefunded)
                .collect(Collectors.toList());

        assertThat(channelGeneratorsRefunded, everyItem(is(false)));
    }

    public static void assertChannelAccountsAreMarkedAsNotRefunded(Long votingId) {
        List<JpaVotingChannelAccount> channelAccounts = Ebean.find(JpaVotingChannelAccount.class)
                .where()
                .eq("voting.id", votingId)
                .findList();

        List<Boolean> channelAccountsConsumed = channelAccounts.stream()
                .map(JpaVotingChannelAccount::isRefunded)
                .collect(Collectors.toList());

        assertThat(channelAccountsConsumed, everyItem(is(false)));
    }

    public static void assertDistributionAccountIsNotMarkedAsRefunded(Long votingId) {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);
        assertThat(voting.isDistributionRefunded(), is(false));
    }

    public static void assertUserGivenFundingAccountExist(Long votingId) {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);
        assertNotNull(voting.getUserGivenFundingAccountPublic());
        assertNotNull(voting.getUserGivenFundingAccountSecret());
    }

    private static List<JpaChannelAccountProgress> channelProgressesOf(Long votingId) {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);
        return voting.getChannelGeneratorAccounts().stream()
                .map(JpaChannelGeneratorAccount::getChannelAccountProgress)
                .collect(Collectors.toList());
    }

    private static List<JpaVotingChannelAccount> channelAccountsOf(Long votingId) {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);
        return voting.getChannelAccounts();
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

