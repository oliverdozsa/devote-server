package data.repositories.imp.voting;

import data.entities.voting.JpaChannelGeneratorAccount;
import data.repositories.voting.ChannelGeneratorAccountRepository;
import io.ebean.EbeanServer;
import play.Logger;

import javax.inject.Inject;
import java.util.List;

public class EbeanChannelGeneratorAccountRepository implements ChannelGeneratorAccountRepository {
    private static final Logger.ALogger logger = Logger.of(EbeanChannelGeneratorAccountRepository.class);

    private final EbeanServer ebeanServer;

    @Inject
    public EbeanChannelGeneratorAccountRepository(EbeanServer ebeanServer) {
        this.ebeanServer = ebeanServer;
    }

    @Override
    public List<JpaChannelGeneratorAccount> getBatchOfNotRefundedOf(Long votingId, int batchSize) {
        logger.info("getBatchOfNotRefundedOf(): votingId = {}, batchSize = {}", votingId, batchSize);

        return ebeanServer.createQuery(JpaChannelGeneratorAccount.class)
                .where()
                .eq("voting.id", votingId)
                .eq("isRefunded", false)
                .setMaxRows(batchSize)
                .findList();
    }

    @Override
    public void channelGeneratorAccountsRefunded(List<Long> refundedAccounts) {
        logger.info("channelGeneratorAccountsRefunded(): refundedAccounts = {}", refundedAccounts);

        List<JpaChannelGeneratorAccount> channelsToBeRefunded = ebeanServer.createQuery(JpaChannelGeneratorAccount.class)
                .where()
                .in("id", refundedAccounts)
                .findList();

        channelsToBeRefunded.forEach(c -> {
            c.setRefunded(true);
            ebeanServer.update(c);
        });
    }
}
