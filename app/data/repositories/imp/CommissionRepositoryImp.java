package data.repositories.imp;

import data.entities.JpaCommissionSession;
import data.entities.JpaVoting;
import data.repositories.CommissionRepository;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.Optional;

import static data.repositories.imp.EbeanRepositoryUtils.assertEntityExists;

public class CommissionRepositoryImp implements CommissionRepository {
    private final EbeanServer ebeanServer;

    private static final Logger.ALogger logger = Logger.of(CommissionRepositoryImp.class);

    @Inject
    public CommissionRepositoryImp(EbeanConfig ebeanConfig) {
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
        // TODO
        return null;
    }

    @Override
    public void storeEnvelopeSignature(String userId, Long votingId, String signature) {
        // TODO
    }
}
