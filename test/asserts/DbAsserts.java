package asserts;

import data.entities.JpaChannelAccountProgress;
import data.entities.JpaVoting;
import data.entities.JpaVotingChannelAccount;
import data.entities.JpaVotingIssuerAccount;
import io.ebean.Ebean;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

public class DbAsserts {
    public static void assertChannelProgressCompletedFor(Long votingId) {
        int totalChannelAccounts = 0;

        List<JpaChannelAccountProgress> channelProgresses = channelProgressesOf(votingId);
        for(JpaChannelAccountProgress p: channelProgresses) {
            assertThat("accounts left to create", p.getNumOfAccountsToLeftToCreate(), equalTo(0L));
            totalChannelAccounts += p.getNumOfAccountsToCreate();
        }

        assertThat("total accounts created", channelAccountsOf(votingId), hasSize(totalChannelAccounts));
    }

    public static void assertVoteTokensAreSavedInDb(Long votingId) {
        List<String> voteTokens = voteTokensOf(votingId);
        assertThat(voteTokens, notNullValue());
        assertThat(voteTokens, hasSize(greaterThan(0)));
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
}
