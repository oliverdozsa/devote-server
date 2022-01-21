package data.repositories.imp;

import data.entities.JpaChannelAccountProgress;
import data.entities.JpaVoting;
import data.entities.JpaVotingIssuerAccount;
import data.repositories.ChannelProgressRepository;
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
        logger.info("notFinishedSampleOf(): sampleSize = {}", sampleSize);

        return ebeanServer.createQuery(JpaChannelAccountProgress.class)
                .where()
                .gt("numOfAccountsLeftToCreate", 0)
                .setMaxRows(sampleSize)
                .findList();
    }

    @Override
    public void issuersCreated(Long votingId) {
        JpaVoting voting = ebeanServer.find(JpaVoting.class, votingId);
        List<JpaVotingIssuerAccount> issuers = voting.getIssuerAccounts();

        long numOfChannelAccountsToCreateForOneIssuer = voting.getVotesCap() / issuers.size();
        long remainderChannelAccountsToCreate = voting.getVotesCap() % issuers.size();

        logger.info("issuersCreated(): Total channel accounts to create: {}", voting.getVotesCap());
        logger.info("issuersCreated(): Creating {} channel progresses for voting with id = {}.",
                issuers.size(), votingId);
        logger.info("issuersCreated(): Number of channel accounts to create per issuer: {}. Remainder channel accounts to create: {}",
                numOfChannelAccountsToCreateForOneIssuer, remainderChannelAccountsToCreate);

        List<JpaChannelAccountProgress> progresses = issuers.stream()
                .map(i -> fromIssuer(i, numOfChannelAccountsToCreateForOneIssuer))
                .collect(Collectors.toList());

        JpaChannelAccountProgress last = progresses.get(progresses.size() - 1);
        last.setNumOfAccountsLeftToCreate(numOfChannelAccountsToCreateForOneIssuer + remainderChannelAccountsToCreate);
        last.setNumOfAccountsToCreate(numOfChannelAccountsToCreateForOneIssuer + remainderChannelAccountsToCreate);

        progresses.forEach(ebeanServer::save);
    }

    private JpaChannelAccountProgress fromIssuer(JpaVotingIssuerAccount issuer, long numOfAccountsToCreate) {
        JpaChannelAccountProgress progress = new JpaChannelAccountProgress();
        progress.setIssuer(issuer);
        progress.setNumOfAccountsToCreate(numOfAccountsToCreate);
        progress.setNumOfAccountsLeftToCreate(numOfAccountsToCreate);
        return progress;
    }
}
