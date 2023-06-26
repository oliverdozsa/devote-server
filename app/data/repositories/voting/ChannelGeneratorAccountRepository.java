package data.repositories.voting;

import data.entities.voting.JpaChannelGeneratorAccount;

import java.util.List;

public interface ChannelGeneratorAccountRepository {
    List<JpaChannelGeneratorAccount> getBatchOfNotRefundedOf(Long votingId, int batchSize);
    void channelGeneratorAccountsRefunded(List<Long> refundedAccounts);
}
