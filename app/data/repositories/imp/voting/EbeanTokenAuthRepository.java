package data.repositories.imp.voting;

import data.entities.voting.JpaAuthToken;
import data.entities.voting.JpaVoter;
import data.entities.voting.JpaVoting;
import data.repositories.voting.TokenAuthRepository;
import exceptions.NotFoundException;
import io.ebean.EbeanServer;
import play.Logger;
import utils.StringUtils;

import javax.inject.Inject;

import java.time.Duration;
import java.time.Instant;

import static data.repositories.imp.EbeanRepositoryUtils.assertEntityExists;

public class EbeanTokenAuthRepository implements TokenAuthRepository {
    private final EbeanServer ebeanServer;

    private static final Logger.ALogger logger = Logger.of(EbeanTokenAuthRepository.class);

    @Inject
    public EbeanTokenAuthRepository(EbeanServer ebeanServer) {
        this.ebeanServer = ebeanServer;
    }

    @Override
    public JpaAuthToken findBy(String token) {
        logger.info("findBy(): token = {}", StringUtils.redactWithEllipsis(token, 5));

        JpaAuthToken authToken = ebeanServer.find(JpaAuthToken.class, token);

        if (authToken == null) {
            throw new NotFoundException("Not found auth token: " + StringUtils.redactWithEllipsis(token, 5));
        }

        return authToken;
    }

    @Override
    public JpaAuthToken createFor(Long votingId, Long voterId) {
        logger.info("createFor(): votingId = {}, voterId = {}", votingId, voterId);

        assertEntityExists(ebeanServer, JpaVoting.class, votingId);
        assertEntityExists(ebeanServer, JpaVoter.class, voterId);

        JpaVoting voting = ebeanServer.find(JpaVoting.class, votingId);
        JpaVoter voter = ebeanServer.find(JpaVoter.class, voterId);

        JpaAuthToken authToken = new JpaAuthToken();
        authToken.setVoter(voter);
        authToken.setVoting(voting);

        ebeanServer.save(authToken);

        return authToken;
    }

    @Override
    public void cleanupOlderThanFromVotingEndDate(int days, int limit) {
        ebeanServer.createQuery(JpaAuthToken.class)
                .where()
                .lt("voting.endDate", Instant.now().minus(Duration.ofDays(days)))
                .setMaxRows(limit)
                .delete();
    }
}
