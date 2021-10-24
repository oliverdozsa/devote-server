package asserts;

import data.entities.JpaVoting;
import data.entities.JpaVotingIssuer;
import devote.blockchain.mockblockchain.MockBlockchainChannelAccount;
import devote.blockchain.mockblockchain.MockBlockchainIssuerAccount;
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
        assertThat(accounts, hasSize(MockBlockchainIssuerAccount.NUM_OF_ISSUER_ACCOUNTS_TO_CREATE));
        accounts.forEach(BlockchainAsserts::assertIssuerAccountCreatedOnBlockchain);
    }

    private static void assertChannelAccountCreatedOnBlockchain(String account) {
        assertTrue(MockBlockchainChannelAccount.isCreated(account));
    }

    private static void assertIssuerAccountCreatedOnBlockchain(String account) {
        assertTrue(MockBlockchainIssuerAccount.isCreated(account));
    }

    private static List<String> issuerAccountsOf(Long votingId) {
        JpaVoting entity = Ebean.find(JpaVoting.class, votingId);
        return entity.getIssuers().stream()
                .map(JpaVotingIssuer::getAccountSecret)
                .collect(Collectors.toList());
    }

    private static List<String> channelAccountsOf(Long votingId) {
        // TODO
        return null;
    }

    private static int votesCapOf(Long votingId) {
        JpaVoting entity = Ebean.find(JpaVoting.class, votingId);
        return entity.getVotesCap().intValue();
    }
}
