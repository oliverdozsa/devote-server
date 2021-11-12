package data.repositories.imp;

import data.entities.JpaVoting;
import data.entities.JpaVotingChannelAccount;
import data.entities.JpaVotingIssuerAccount;
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
        logger.info("issuerAccountsCreated(): id = {}, accounts size = {}", id, accounts.size());

        JpaVoting voting = ebeanServer.find(JpaVoting.class, id);

        List<JpaVotingIssuerAccount> votingIssuers = accounts.stream()
                .map(this::fromIssuerAccountSecret)
                .collect(Collectors.toList());

        voting.setIssuerAccounts(votingIssuers);
        ebeanServer.merge(voting);
    }

    @Override
    public void channelAccountCreated(Long id, List<String> accounts) {
        logger.info("channelAccountCreated(): id = {}, accounts size = {}", id, accounts.size());

        JpaVoting voting = ebeanServer.find(JpaVoting.class, id);

        List<JpaVotingChannelAccount> channelAccounts = accounts.stream()
                .map(this::fromChannelAccountSecret)
                .collect(Collectors.toList());

        voting.setChannelAccounts(channelAccounts);
        ebeanServer.merge(voting);
    }

    @Override
    public void distributionAndBallotAccountsCreated(Long id, String distributionSecret, String ballotSecret) {
        JpaVoting voting = ebeanServer.find(JpaVoting.class, id);
        voting.setDistributionAccountSecret(distributionSecret);
        voting.setBallotAccountSecret(ballotSecret);

        ebeanServer.update(voting);
    }

    private static JpaVoting fromRequest(CreateVotingRequest request) {
        JpaVoting entity = new JpaVoting();
        entity.setNetwork(request.getNetwork());
        entity.setVotesCap(request.getVotesCap());

        return entity;
    }

    private JpaVotingIssuerAccount fromIssuerAccountSecret(String account) {
        JpaVotingIssuerAccount votingIssuer = new JpaVotingIssuerAccount();
        votingIssuer.setAccountSecret(account);
        return votingIssuer;
    }

    private JpaVotingChannelAccount fromChannelAccountSecret(String account) {
        JpaVotingChannelAccount votingChannelAccount = new JpaVotingChannelAccount();
        votingChannelAccount.setAccountSecret(account);
        return votingChannelAccount;
    }
}
