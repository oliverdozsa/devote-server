package asserts;

import data.entities.JpaChannelAccountProgress;
import data.entities.JpaVoting;
import data.entities.JpaVotingChannelAccount;
import data.entities.JpaVotingIssuer;
import io.ebean.Ebean;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class DbAsserts {
    public static void assertChannelProgressCompletedFor(Long votingId) {
        int totalChannelAccounts = 0;

        List<JpaChannelAccountProgress> channelProgresses = channelProgressesOf(votingId);
        for(JpaChannelAccountProgress p: channelProgresses) {
            assertThat("accounts left to create", p.getNumOfAccountsToLeftToCreate(), equalTo(0));
            totalChannelAccounts += p.getNumOfAccountsToCreate();
        }

        assertThat("total accounts created", channelAccountsOf(votingId), hasSize(totalChannelAccounts));
    }

    private static List<JpaChannelAccountProgress> channelProgressesOf(Long votingId) {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);
        return voting.getIssuers().stream()
                .map(JpaVotingIssuer::getChannelAccountProgress)
                .collect(Collectors.toList());
    }

    private static List<JpaVotingChannelAccount> channelAccountsOf(Long votingId) {
        JpaVoting voting = Ebean.find(JpaVoting.class, votingId);
        return voting.getChannelAccounts();
    }
}
