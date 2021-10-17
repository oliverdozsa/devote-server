package data.repositories.imp;

import data.entities.JpaVoting;
import data.repositories.VotingRepository;
import dto.CreateVotingRequest;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;

public class EbeanVotingRepository implements VotingRepository {
    private static final Logger.ALogger logger = Logger.of(EbeanVotingRepository.class);

    private final EbeanServer ebeanServer;

    @Inject
    public EbeanVotingRepository(EbeanConfig ebeanConfig) {
        ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
    }
    @Override
    public Long initalize(CreateVotingRequest request) {
        logger.info("initialize(): createVotingDto = {}", request);

        JpaVoting voting = fromRequest(request);
        ebeanServer.save(voting);
        return voting.getId();
    }

    @Override
    public JpaVoting byId(Long id) {
        return ebeanServer.find(JpaVoting.class, id);
    }

    private static JpaVoting fromRequest(CreateVotingRequest request) {
        JpaVoting entity = new JpaVoting();
        entity.setNetwork(request.getNetwork());

        return entity;
    }
}
