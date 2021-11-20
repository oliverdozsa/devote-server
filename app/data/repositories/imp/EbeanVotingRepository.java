package data.repositories.imp;

import data.entities.JpaVoting;
import data.entities.JpaVotingChannelAccount;
import data.entities.JpaVotingIssuerAccount;
import data.repositories.VotingRepository;
import devote.blockchain.api.DistributionAndBallotAccount;
import dto.CreateVotingRequest;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static data.repositories.imp.EbeanVotingInit.initVotingFrom;

public class EbeanVotingRepository implements VotingRepository {
    private static final Logger.ALogger logger = Logger.of(EbeanVotingRepository.class);

    private final EbeanServer ebeanServer;

    @Inject
    public EbeanVotingRepository(EbeanConfig ebeanConfig) {
        ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
    }

    @Override
    public Long initialize(CreateVotingRequest request) {
        logger.info("initialize(): request = {}", request);

        JpaVoting voting = initVotingFrom(request);

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
    public void distributionAndBallotAccountsCreated(Long id, DistributionAndBallotAccount.TransactionResult transactionResult) {
        JpaVoting voting = ebeanServer.find(JpaVoting.class, id);
        voting.setDistributionAccountSecret(transactionResult.distributionSecret);
        voting.setBallotAccountSecret(transactionResult.ballotSecret);

        ebeanServer.update(voting);

        Map<String, JpaVotingIssuerAccount> votingIssuersBySecret = new HashMap<>();
        voting.getIssuerAccounts().forEach(i -> votingIssuersBySecret.put(i.getAccountSecret(), i));

        transactionResult.issuerTokens.forEach((s, t) -> {
            JpaVotingIssuerAccount votingIssuer = votingIssuersBySecret.get(s);
            votingIssuer.setAssetCode(t);
            ebeanServer.update(votingIssuer);
        });
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
