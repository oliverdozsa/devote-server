package data.repositories.imp;

import data.entities.JpaVoting;
import data.entities.JpaVotingIssuer;
import data.repositories.VotingRepository;
import dto.CreateVotingRequest;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class EbeanVotingRepository implements VotingRepository {
    private static final Logger.ALogger logger = Logger.of(EbeanVotingRepository.class);

    private final EbeanServer ebeanServer;

    @Inject
    public EbeanVotingRepository(EbeanConfig ebeanConfig) {
        ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
    }

    @Override
    public Long initialize(CreateVotingRequest request) {
        logger.info("initialize(): createVotingDto = {}", request);

        JpaVoting voting = fromRequest(request);
        voting.setCreatedAt(Instant.now());

        ebeanServer.save(voting);
        return voting.getId();
    }

    @Override
    public JpaVoting single(Long id) {
        logger.info("single(): id = {}", id);
        return ebeanServer.find(JpaVoting.class, id);
    }

    @Override
    public void issuerAccountsCreated(Long id, List<String> accounts) {
        JpaVoting voting = ebeanServer.find(JpaVoting.class, id);

        List<JpaVotingIssuer> votingIssuers = accounts.stream()
                .map(this::fromAccount)
                .collect(Collectors.toList());

        voting.setIssuers(votingIssuers);
        ebeanServer.merge(voting);
    }

    private static JpaVoting fromRequest(CreateVotingRequest request) {
        JpaVoting entity = new JpaVoting();
        entity.setNetwork(request.getNetwork());
        entity.setVotesCap(request.getVotesCap());

        return entity;
    }

    private JpaVotingIssuer fromAccount(String account) {
        JpaVotingIssuer votingIssuer = new JpaVotingIssuer();
        votingIssuer.setAccountSecret(account);
        return votingIssuer;
    }
}
