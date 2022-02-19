package data.repositories.imp;

import data.entities.JpaVoter;
import data.repositories.VoterRepository;
import exceptions.NotFoundException;
import io.ebean.EbeanServer;
import play.Logger;

import javax.inject.Inject;

public class EbeanVoterRepository implements VoterRepository {
    private final EbeanServer ebeanServer;

    private static final Logger.ALogger logger = Logger.of(EbeanVoterRepository.class);

    @Inject
    public EbeanVoterRepository(EbeanServer ebeanServer) {
        this.ebeanServer = ebeanServer;
    }

    @Override
    public void setUserIdForEmail(String email, String userId) {
        logger.info("setUserIdForEmail(): email = {}, userId = {}", email, userId);

        JpaVoter voter = ebeanServer.createQuery(JpaVoter.class)
                .where()
                .eq("email", email)
                .findOne();

        if (voter == null) {
            logger.warn("setUserIdForEmail(): not found voter for email: {}", email);
            throw new NotFoundException("Not found voter for email: " + email);
        }

        voter.setUserId(userId);
        ebeanServer.update(voter);
    }

    @Override
    public JpaVoter getVoterByUserId(String userId) {
        logger.info("getVoterByUserId(): userId = {}", userId);
        return ebeanServer.createQuery(JpaVoter.class)
                .where()
                .eq("userId", userId)
                .findOne();
    }

    @Override
    public boolean doesParticipateInVoting(String userId, Long votingId) {
        logger.info("doesParticipateInVoting(): userId = {}, votingId = {}", userId, votingId);

        JpaVoter voter = ebeanServer.createQuery(JpaVoter.class)
                .where()
                .eq("userId", userId)
                .eq("votings.id", votingId)
                .findOne();

        return voter != null;
    }
}
