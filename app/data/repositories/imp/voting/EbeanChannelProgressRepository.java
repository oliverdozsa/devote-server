package data.repositories.imp.voting;

import data.entities.voting.JpaChannelAccountProgress;
import data.entities.voting.JpaVoting;
import data.entities.voting.JpaChannelGeneratorAccount;
import data.repositories.voting.ChannelProgressRepository;
import io.ebean.EbeanServer;
import play.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class EbeanChannelProgressRepository implements ChannelProgressRepository {
    private static final Logger.ALogger logger = Logger.of(EbeanChannelProgressRepository.class);

    private final EbeanServer ebeanServer;

    @Inject
    public EbeanChannelProgressRepository(EbeanServer ebeanServer) {
        this.ebeanServer = ebeanServer;
    }

    @Override
    public void channelAccountsCreated(Long id, int numOfAccountsCreated) {
        logger.info("channelAccountsCreated(): id = {}, numOfAccountsCreated = {}", id, numOfAccountsCreated);

        JpaChannelAccountProgress channelProgress = ebeanServer.find(JpaChannelAccountProgress.class, id);

        channelProgress.setNumOfAccountsLeftToCreate(
                channelProgress.getNumOfAccountsLeftToCreate() - numOfAccountsCreated
        );

        if (channelProgress.getNumOfAccountsLeftToCreate() == 0) {
            logger.info("channelAccountsCreated(): channel progress with id = {} finished!", id);
        }

        ebeanServer.update(channelProgress);
    }

    @Override
    public List<JpaChannelAccountProgress> notFinishedSampleOf(int sampleSize) {
        logger.debug("notFinishedSampleOf(): sampleSize = {}", sampleSize);

        return ebeanServer.createQuery(JpaChannelAccountProgress.class)
                .where()
                .gt("numOfAccountsLeftToCreate", 0)
                .setMaxRows(sampleSize)
                .findList();
    }

    @Override
    public void channelGeneratorsCreated(Long votingId) {
        JpaVoting voting = ebeanServer.find(JpaVoting.class, votingId);
        List<JpaChannelGeneratorAccount> channelGenerators = voting.getChannelGeneratorAccounts();

        logger.info("channelGeneratorsCreated(): Total channel accounts to create: {}", voting.getVotesCap());
        logger.info("channelGeneratorsCreated(): Creating {} channel progresses for voting with id = {}.",
                channelGenerators.size(), votingId);
        List<Long> votesCapOfChannelGenerators = channelGenerators.stream()
                .map(JpaChannelGeneratorAccount::getVotesCap)
                .collect(Collectors.toList());
        logger.info("channelGeneratorsCreated(): Number of channel accounts to create in each bucket: {}", votesCapOfChannelGenerators);

        List<JpaChannelAccountProgress> progresses = channelGenerators.stream()
                .map(this::fromChannelGenerator)
                .collect(Collectors.toList());

        progresses.forEach(ebeanServer::save);
    }

    private JpaChannelAccountProgress fromChannelGenerator(JpaChannelGeneratorAccount channelGenerator) {
        JpaChannelAccountProgress progress = new JpaChannelAccountProgress();
        progress.setChannelGenerator(channelGenerator);
        progress.setNumOfAccountsToCreate(channelGenerator.getVotesCap());
        progress.setNumOfAccountsLeftToCreate(channelGenerator.getVotesCap());
        return progress;
    }
}
