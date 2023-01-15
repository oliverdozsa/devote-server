package data.repositories.imp;

import data.entities.JpaAuthToken;
import data.entities.JpaVoting;
import data.entities.Visibility;
import data.repositories.PageOfVotingsRepository;
import io.ebean.EbeanServer;
import io.ebean.Query;
import responses.Page;
import security.TokenAuthUserIdUtil;

import javax.inject.Inject;
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

        query.where().eq("visibility", Visibility.PUBLIC.name());
        offsetAndLimit(query, offset, limit);

        return toPage(query);
    }

    @Override
    public Page<JpaVoting> votingsOfVoteCaller(int offset, int limit, String userId) {
        Query<JpaVoting> query = ebeanServer.createQuery(JpaVoting.class);

        query.where().eq("createdBy", userId);
        offsetAndLimit(query, offset, limit);

        return toPage(query);
    }

    @Override
    public Page<JpaVoting> votingsOfVoter(int offset, int limit, String userId) {
        if (tokenAuthUserIdUtil.isForTokenAuth(userId)) {
            assertEntityExists(ebeanServer, JpaAuthToken.class, tokenAuthUserIdUtil.getTokenFrom(userId));

            return findVotingForTokenUserId(userId);
        }

        Query<JpaVoting> query = ebeanServer.createQuery(JpaVoting.class);

        query.where().eq("voters.voterIds.id", userId);
        offsetAndLimit(query, offset, limit);

        return toPage(query);
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
}
