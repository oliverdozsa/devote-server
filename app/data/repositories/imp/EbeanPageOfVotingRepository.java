package data.repositories.imp;

import data.entities.JpaVoting;
import data.entities.Visibility;
import data.repositories.PageOfVotingsRepository;
import io.ebean.EbeanServer;
import io.ebean.Query;
import responses.Page;

import javax.inject.Inject;

public class EbeanPageOfVotingRepository implements PageOfVotingsRepository {
    private final EbeanServer ebeanServer;

    @Inject
    public EbeanPageOfVotingRepository(EbeanServer ebeanServer) {
        this.ebeanServer = ebeanServer;
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
        Query<JpaVoting> query = ebeanServer.createQuery(JpaVoting.class);

        query.where().eq("voters.userId", userId);
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
}
