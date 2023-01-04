package data.repositories;

import data.entities.JpaChannelGeneratorAccount;

import java.util.List;

public interface ChannelGeneratorAccountRepository {
    List<JpaChannelGeneratorAccount> getBatchOfNotRefundedOf(Long votingId, int batchSize);
    void channelGeneratorAccountsRefunded(List<Long> refundedAccounts);
}
