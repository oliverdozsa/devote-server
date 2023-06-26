package data.repositories.imp.voting;

import data.entities.voting.JpaAuthToken;
import data.entities.voting.JpaVoter;
import data.entities.voting.JpaVoterUserId;
import data.repositories.voting.VoterRepository;
import io.ebean.EbeanServer;
import io.ebean.Query;
import play.Logger;
import security.TokenAuthUserIdUtil;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.ebean.Expr.in;

public class EbeanVoterRepository implements VoterRepository {
    private final EbeanServer ebeanServer;
    private final TokenAuthUserIdUtil tokenAuthUserIdUtil;

    private static final Logger.ALogger logger = Logger.of(EbeanVoterRepository.class);

    @Inject
    public EbeanVoterRepository(EbeanServer ebeanServer, TokenAuthUserIdUtil tokenAuthUserIdUtil) {
        this.ebeanServer = ebeanServer;
        this.tokenAuthUserIdUtil = tokenAuthUserIdUtil;
    }

    @Override
    public void userAuthenticated(String email, String userId) {
        logger.info("userAuthenticated(): email = {}, userId = {}", email, userId);

        if ((email == null || email.length() == 0) && (userId == null || userId.length() == 0)) {
            throw new IllegalArgumentException("Both email and userId is empty! At least one should have value");
        }

        if(tokenAuthUserIdUtil.isForTokenAuth(userId)) {
            logger.info("userAuthenticated(): user id is for token auth; skipping voter creation.");
            return;
        }

        if (email != null && email.length() > 0) {
            logger.info("userAuthenticated(): email is present; creating a new voter, and / or attaching user id as needed.");
            createVoterByEmailAndUserIdIfNeeded(email, userId);
        } else {
            logger.info("userAuthenticated(): email is empty; finding, or creating voter by user id.");
            createVoterByUserIdOnlyIfNeeded(userId);
        }
    }

    @Override
    public boolean doesParticipateInVoting(String userId, Long votingId) {
        logger.info("doesParticipateInVoting(): userId = {}, votingId = {}", userId, votingId);

        if(tokenAuthUserIdUtil.isForTokenAuth(userId)) {
            UUID token = tokenAuthUserIdUtil.getTokenFrom(userId);
            Optional<JpaAuthToken> optionalAuthToken = ebeanServer.createQuery(JpaAuthToken.class)
                    .where()
                    .eq("token", token)
                    .eq("voting.id", votingId)
                    .findOneOrEmpty();

            return optionalAuthToken.isPresent();
        }

        JpaVoter voter = ebeanServer.createQuery(JpaVoter.class)
                .where()
                .eq("voterIds.id", userId)
                .eq("votings.id", votingId)
                .findOne();

        return voter != null;
    }

    @Override
    public List<JpaVoter> findThoseWhoNeedsAuthToken(Long votingId, int limit) {
        logger.info("findThoseWhoNeedsAuthToken(): votingId = {}, limit = {}", votingId, limit);

        Query<JpaAuthToken> authTokensOfVotingQuery = ebeanServer.createQuery(JpaAuthToken.class)
                .select("voter.id")
                .where()
                .eq("voting.id", votingId)
                .query();

        return ebeanServer.createQuery(JpaVoter.class)
                .where()
                .eq("votings.id", votingId)
                .not(in("id", authTokensOfVotingQuery))
                .setMaxRows(limit)
                .findList();
    }

    private void createVoterByEmailAndUserIdIfNeeded(String email, String userId) {
        Optional<JpaVoter> optionalJpaVoterByUserId = findVoterByUserId(userId);
        if (optionalJpaVoterByUserId.isPresent()) {
            logger.info("createVoterByEmailAndUserIdIfNeeded(): Already have a voter with user id = {};" +
                    " updating email of them", userId);
            JpaVoter voter = optionalJpaVoterByUserId.get();
            voter.setEmail(email);
            ebeanServer.update(voter);
        } else {
            logger.info("createVoterByEmailAndUserIdIfNeeded(): Not found voter with user id = {}; " +
                    "try finding it by email, and adding user id if needed.", userId);
            JpaVoter voterByEmail = findOrCreateVoterByEmail(email);
            addUserIdForVoterIfNeeded(voterByEmail, userId);
        }
    }

    private JpaVoter findOrCreateVoterByEmail(String email) {
        Optional<JpaVoter> optionalJpaVoter = findVoterByEmail(email);
        if (optionalJpaVoter.isPresent()) {
            logger.info("findOrCreateVoterByEmail(): user with email: {} already exists", email);
            return optionalJpaVoter.get();
        } else {
            logger.info("findOrCreateVoterByEmail(): creating user with email: {}", email);
            JpaVoter voter = new JpaVoter();
            voter.setEmail(email);
            ebeanServer.save(voter);

            return voter;
        }
    }

    private Optional<JpaVoter> findVoterByEmail(String email) {
        return ebeanServer.createQuery(JpaVoter.class)
                .where()
                .eq("email", email)
                .findOneOrEmpty();
    }

    private void createVoterByUserIdOnlyIfNeeded(String userId) {
        Optional<JpaVoter> optionalJpaVoter = findVoterByUserId(userId);

        if (!optionalJpaVoter.isPresent()) {
            JpaVoter voter = new JpaVoter();
            ebeanServer.save(voter);

            JpaVoterUserId voterId = new JpaVoterUserId();
            voterId.setId(userId);
            voterId.setVoter(voter);

            ebeanServer.save(voterId);
        }
    }

    private Optional<JpaVoter> findVoterByUserId(String userId) {
        if (userId == null || userId.length() == 0) {
            return Optional.empty();
        }

        JpaVoterUserId voterUserId = ebeanServer.find(JpaVoterUserId.class, userId);

        if (voterUserId == null) {
            return Optional.empty();
        } else {
            return Optional.of(voterUserId.getVoter());
        }
    }

    private void addUserIdForVoterIfNeeded(JpaVoter voter, String userId) {
        if (userId == null || userId.length() == 0) {
            logger.info("addUserIdForVoterIfNeeded(): user id is empty; nothing to add.");
            return;
        }

        logger.info("addUserIdForVoterIfNeeded(): voter.id = {}, userId = {}", voter.getId(), userId);

        Optional<JpaVoterUserId> voterIdOptional = ebeanServer.createQuery(JpaVoterUserId.class)
                .where()
                .eq("id", userId)
                .findOneOrEmpty();

        if (!voterIdOptional.isPresent()) {
            logger.info("addUserIdForVoterIfNeeded(): user id is not yet added; adding it.");
            JpaVoterUserId voterId = new JpaVoterUserId();
            voterId.setId(userId);
            voterId.setVoter(voter);

            ebeanServer.save(voterId);
        } else {
            logger.info("addUserIdForVoterIfNeeded(): already have user id; nothing to do", userId);
        }
    }
}
