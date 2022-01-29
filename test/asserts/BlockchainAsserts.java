package asserts;

import data.entities.JpaVoting;
import data.entities.JpaVotingChannelAccount;
import data.entities.JpaVotingIssuerAccount;
import devote.blockchain.mockblockchain.MockBlockchainChannelAccountOperation;
import devote.blockchain.mockblockchain.MockBlockchainDistributionAndBallotAccountOperation;
import devote.blockchain.mockblockchain.MockBlockchainIssuerAccountOperation;
import io.ebean.Ebean;

import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

public class BlockchainAsserts {
    public static void assertChannelAccountsCreatedOnBlockchain(Long votingId) {
        List<String> accounts = channelAccountsOf(votingId);
        assertThat(accounts, not(empty()));
        assertThat(accounts, hasSize(votesCapOf(votingId)));
        accounts.forEach(BlockchainAsserts::assertChannelAccountCreatedOnBlockchain);
    }

    public static void assertIssuerAccountsCreatedOnBlockchain(Long votingId) {
        List<String> accounts = issuerAccountsOf(votingId);
        assertThat(accounts, not(empty()));
        assertThat(accounts, hasSize(MockBlockchainIssuerAccountOperation.NUM_OF_ISSUER_ACCOUNTS_TO_CREATE));
        accounts.forEach(BlockchainAsserts::assertIssuerAccountCreatedOnBlockchain);
    }

    public static void assertDistributionAndBallotAccountsCreatedOnBlockchain(Long votingId) {
        String distributionAccount = distributionAccountOf(votingId);
        assertDistributionAccountCreated(distributionAccount);

        String ballotAccount = ballotAccountOf(votingId);
        assertBallotAccountCreated(ballotAccount);
    }

    private static void assertChannelAccountCreatedOnBlockchain(String account) {
        assertTrue("Channel account: " + account + " is not created!", MockBlockchainChannelAccountOperation.isCreated(account));
    }

    private static void assertIssuerAccountCreatedOnBlockchain(String account) {
        assertTrue("Issuer account: " + account + " is not created!", MockBlockchainIssuerAccountOperation.isCreated(account));
    }

    private static List<String> issuerAccountsOf(Long votingId) {
        JpaVoting entity = Ebean.find(JpaVoting.class, votingId);
        return entity.getIssuerAccounts().stream()
                .map(JpaVotingIssuerAccount::getAccountSecret)
                .collect(Collectors.toList());
    }

    private static List<String> channelAccountsOf(Long votingId) {
        JpaVoting entity = Ebean.find(JpaVoting.class, votingId);

        return entity.getChannelAccounts().stream()
                .map(JpaVotingChannelAccount::getAccountSecret)
                .collect(Collectors.toList());
    }

    private static int votesCapOf(Long votingId) {
        JpaVoting entity = Ebean.find(JpaVoting.class, votingId);
        return entity.getVotesCap().intValue();
    }

    private static String ballotAccountOf(Long votingId) {
        JpaVoting entity = Ebean.find(JpaVoting.class, votingId);
        return entity.getBallotAccountSecret();
    }

    private static String distributionAccountOf(Long votingId) {
        JpaVoting entity = Ebean.find(JpaVoting.class, votingId);
        return entity.getDistributionAccountSecret();
    }

    private static void assertDistributionAccountCreated(String account) {
        assertTrue("Distribution account: " + account + " is not created!", MockBlockchainDistributionAndBallotAccountOperation.isDistributionAccountCreated(account));
    }

    private static void assertBallotAccountCreated(String account) {
        assertTrue("Ballot account: " + account + " is not created!", MockBlockchainDistributionAndBallotAccountOperation.isBallotAccountCreated(account));
    }
}
