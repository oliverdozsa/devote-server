package data.repositories.imp.voting;

import data.entities.voting.*;
import data.repositories.voting.PageOfVotingsRepository;
import io.ebean.EbeanServer;
import io.ebean.Query;
import responses.Page;
import security.TokenAuthUserIdUtil;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static data.repositories.imp.EbeanRepositoryUtils.assertEntityExists;

public class EbeanPageOfVotingRepository implements PageOfVotingsRepository {
    private final EbeanServer ebeanServer;
    private final TokenAuthUserIdUtil tokenAuthUserIdUtil;

    @Inject
    public EbeanPageOfVotingRepository(EbeanServer ebeanServer, TokenAuthUserIdUtil tokenAuthUserIdUtil) {
        this.ebeanServer = ebeanServer;
        this.tokenAuthUserIdUtil = tokenAuthUserIdUtil;
    }

    @Override
    public Page<JpaVoting> pageOfPublic(int offset, int limit) {
        Query<JpaVoting> query = ebeanServer.createQuery(JpaVoting.class);

        query.where()
                .eq("visibility", Visibility.PUBLIC.name())
                .setOrderBy("createdAt desc");
        offsetAndLimit(query, offset, limit);

        return toPage(query);
    }

    @Override
    public Page<JpaVoting> votingsOfVoteCaller(int offset, int limit, String userId) {
        Query<JpaVoting> query = ebeanServer.createQuery(JpaVoting.class);

        query.where()
                .eq("createdBy", userId)
                .setOrderBy("createdAt desc");
        offsetAndLimit(query, offset, limit);

        return toPage(query);
    }

    @Override
    public Page<JpaVoting> votingsOfVoter(int offset, int limit, String userId) {
        Query<JpaVoting> query = votingsOfVoterQuery(offset, limit, userId);
        return votingsOfVoterBasedOn(query, userId);
    }

    @Override
    public Page<JpaVoting> votingsOfVoterFilteredByNotTriedToCastVote(int offset, int limit, String userId) {
        Query<JpaVoting> query = votingsOfVoterQuery(offset, limit, userId);

        JpaVoterUserId voterUserId = ebeanServer.find(JpaVoterUserId.class, userId);

        Query<JpaCommissionSession> votingsOfSessionsOfVoter = ebeanServer.createQuery(JpaCommissionSession.class)
                .select("voting.id")
                .where()
                .eq("voter.id", voterUserId.getVoter().getId())
                .query();

        query.where()
                .gt("endDate", Instant.now())
                .disjunction()
                .isNull("initSessions.id")
                .notIn("id", votingsOfSessionsOfVoter);

        return votingsOfVoterBasedOn(query, userId);
    }

    private void offsetAndLimit(Query<JpaVoting> query, int offset, int limit) {
        query.setFirstRow(offset).setMaxRows(limit);
    }

    private Page<JpaVoting> toPage(Query<JpaVoting> query) {
        Page<JpaVoting> page = new Page<>();
        page.setTotalCount(query.findCount());
        page.setItems(query.findList());

        return page;
    }

    private Page<JpaVoting> findVotingForTokenUserId(String userId) {
        JpaAuthToken jpaAuthToken = ebeanServer.createQuery(JpaAuthToken.class)
                .where()
                .eq("token", tokenAuthUserIdUtil.getTokenFrom(userId))
                .findOne();

        Page<JpaVoting> result = new Page<>();

        List<JpaVoting> votingAsList = Collections.singletonList(jpaAuthToken.getVoting());
        result.setItems(votingAsList);
        result.setTotalCount(1);
        return result;
    }

    private Query<JpaVoting> votingsOfVoterQuery(int offset, int limit, String userId) {
        Query<JpaVoting> query = ebeanServer.createQuery(JpaVoting.class);

        query.where()
                .eq("voters.voterIds.id", userId)
                .setOrderBy("createdAt desc");
        offsetAndLimit(query, offset, limit);

        return query;
    }

    private Page<JpaVoting> votingsOfVoterBasedOn(Query<JpaVoting> query, String userId) {
        if (tokenAuthUserIdUtil.isForTokenAuth(userId)) {
            assertEntityExists(ebeanServer, JpaAuthToken.class, tokenAuthUserIdUtil.getTokenFrom(userId));

            return findVotingForTokenUserId(userId);
        }

        return toPage(query);
    }
}
