package data.repositories.imp;

import data.entities.JpaChannelAccountProgress;
import data.repositories.ChannelProgressRepository;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import play.Logger;
import play.db.ebean.EbeanConfig;

import java.util.List;

public class EbeanChannelProgressRepository implements ChannelProgressRepository {
    private static final Logger.ALogger logger = Logger.of(EbeanChannelProgressRepository.class);

    private final EbeanServer ebeanServer;

    public EbeanChannelProgressRepository(EbeanConfig ebeanConfig) {
        ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
    }

    @Override
    public void channelAccountsCreated(Long id, int numOfAccountsCreated) {
        JpaChannelAccountProgress channelProgress = ebeanServer.find(JpaChannelAccountProgress.class, id);

        channelProgress.setNumOfAccountsToLeftToCreate(
                channelProgress.getNumOfAccountsToLeftToCreate() - numOfAccountsCreated
        );

        ebeanServer.update(channelProgress);
    }

    @Override
    public List<JpaChannelAccountProgress> notFinishedSampleOf(int sampleSize) {
        return ebeanServer.createQuery(JpaChannelAccountProgress.class)
                .where()
                .gt("numOfAccountsToLeftToCreate", 0)
                .setMaxRows(sampleSize)
                .findList();
    }
}
