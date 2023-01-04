package data.repositories.imp;

import data.entities.JpaVotingChannelAccount;
import data.repositories.ChannelAccountRepository;
import io.ebean.EbeanServer;
import play.Logger;

import javax.inject.Inject;
import java.util.List;

public class EbeanChannelAccountRepository implements ChannelAccountRepository {
    private static final Logger.ALogger logger = Logger.of(EbeanChannelAccountRepository.class);

    private final EbeanServer ebeanServer;

    @Inject
    public EbeanChannelAccountRepository(EbeanServer ebeanServer) {
        this.ebeanServer = ebeanServer;
    }

    @Override
    public List<JpaVotingChannelAccount> getBatchOfNotRefundedOf(Long votingId, int batchSize) {
        logger.info("getBatchOfNotRefundedOf(): votingId = {}, batchSize = {}", votingId, batchSize);

        return ebeanServer.createQuery(JpaVotingChannelAccount.class)
                .where()
                .eq("voting.id", votingId)
                .eq("isRefunded", false)
                .setMaxRows(batchSize)
                .findList();
    }

    @Override
    public void channelAccountsRefunded(List<Long> refundedAccounts) {
        logger.info("channelAccountsRefunded(): refundedAccounts = {}", refundedAccounts);

        List<JpaVotingChannelAccount> channelsToBeRefunded = ebeanServer.createQuery(JpaVotingChannelAccount.class)
                .where()
                .in("id", refundedAccounts)
                .findList();

        channelsToBeRefunded.forEach(c -> {
            c.setRefunded(true);
            ebeanServer.update(c);
        });
    }
}
