package data.repositories;

import data.entities.JpaVotingChannelAccount;

import java.util.List;

public interface ChannelAccountRepository {
    List<JpaVotingChannelAccount> getBatchOfNotRefundedOf(Long votingId, int batchSize);
    void channelAccountsRefunded(List<Long> refundedAccounts);
}
