package data.repositories.imp;

import data.entities.JpaCommissionSession;
import data.entities.JpaVoting;
import data.entities.JpaVotingChannelAccount;
import data.entities.JpaVotingIssuerAccount;
import data.repositories.CommissionRepository;
import exceptions.InternalErrorException;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import play.Logger;
import play.db.ebean.EbeanConfig;
import scala.util.Random;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static data.repositories.imp.EbeanRepositoryUtils.assertEntityExists;

public class EbeanCommissionRepository implements CommissionRepository {
    private final EbeanServer ebeanServer;

    private static final Logger.ALogger logger = Logger.of(EbeanCommissionRepository.class);

    @Inject
    public EbeanCommissionRepository(EbeanConfig ebeanConfig) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
    }

    @Override
    public Optional<JpaCommissionSession> getByVotingIdAndUserId(Long votingId, String userId) {
        logger.info("getByVotingAndUserId(): votingId = {}, userId = {}", votingId, userId);

        JpaCommissionSession entity = ebeanServer.createQuery(JpaCommissionSession.class)
                .where()
                .eq("voting.id", votingId)
                .eq("userId", userId)
                .findOne();

        return Optional.ofNullable(entity);
    }

    @Override
    public JpaCommissionSession createSession(Long votingId, String userId) {
        logger.info("createSession(): votingId = {}, userId = {}", votingId, userId);

        assertEntityExists(ebeanServer, JpaVoting.class, votingId);

        JpaCommissionSession initSession = new JpaCommissionSession();
        JpaVoting voting = ebeanServer.find(JpaVoting.class, votingId);
        initSession.setVoting(voting);
        initSession.setUserId(userId);

        ebeanServer.save(initSession);
        return initSession;
    }

    @Override
    public Boolean hasAlreadySignedAnEnvelope(String userId, Long votingId) {
        JpaCommissionSession commissionSession = find(userId, votingId);
        return commissionSession.getEnvelopeSignature() != null;
    }

    @Override
    public void storeEnvelopeSignature(String userId, Long votingId, String signature) {
        JpaCommissionSession commissionSession = find(userId, votingId);

        commissionSession.setEnvelopeSignature(signature);
        ebeanServer.update(commissionSession);
    }

    @Override
    public JpaVotingChannelAccount consumeOneChannel(Long votingId) {
        logger.info("consumeOneChannel(): votingId = {}", votingId);
        Optional<JpaVotingChannelAccount> optionalJpaVotingChannelAccount = ebeanServer.createQuery(JpaVotingChannelAccount.class)
                .where()
                .eq("isConsumed", false)
                .setMaxRows(1)
                .findOneOrEmpty();

        if (optionalJpaVotingChannelAccount.isPresent()) {
            JpaVotingChannelAccount channelAccount = optionalJpaVotingChannelAccount.get();
            channelAccount.setConsumed(true);
            ebeanServer.update(channelAccount);

            logger.info("consumeOneChannel(): success consumed a channel! id = {}", channelAccount.getId());

            return channelAccount;
        } else if (areAllChannelAccountsCreated(votingId)) {
            throw new InternalErrorException("Could not find a free channel account!");
        } else {
            throw new InternalErrorException("Could not find a free channel account! Please try again later!");
        }
    }

    @Override
    public JpaVotingIssuerAccount selectAnIssuer(Long votingId) {
        logger.info("selectAnIssuer(): votingId = {}", votingId);

        assertEntityExists(ebeanServer, JpaVoting.class, votingId);
        List<JpaVotingIssuerAccount> issuers = ebeanServer.createQuery(JpaVotingIssuerAccount.class)
                .where()
                .eq("voting.id", votingId)
                .findList();

        if (issuers == null || issuers.size() == 0) {
            throw new InternalErrorException("Not found any issuer account for voting: " + votingId);
        }

        int randomIssuerIndex = ThreadLocalRandom.current().nextInt(issuers.size());
        JpaVotingIssuerAccount randomIssuer = issuers.get(randomIssuerIndex);

        logger.info("selectAnIssuer(): selected issuer: {}", randomIssuer.getId());
        return issuers.get(randomIssuerIndex);
    }

    @Override
    public void storeTransactionForRevealedSignature(String signature, String transaction) {
        // TODO
    }

    private JpaCommissionSession find(String userId, Long votingId) {
        return ebeanServer.createQuery(JpaCommissionSession.class)
                .where()
                .eq("userId", userId)
                .eq("voting.id", votingId)
                .findOne();
    }

    private boolean areAllChannelAccountsCreated(Long votingId) {
        assertEntityExists(ebeanServer, JpaVoting.class, votingId);
        JpaVoting voting = ebeanServer.find(JpaVoting.class, votingId);

        for (JpaVotingIssuerAccount issuer : voting.getIssuerAccounts()) {
            if (issuer.getChannelAccountProgress().getNumOfAccountsLeftToCreate() > 0) {
                return false;
            }
        }

        return true;
    }
}
