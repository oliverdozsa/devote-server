package data.repositories.imp;

import data.entities.JpaVoting;
import data.entities.JpaVotingChannelAccount;
import data.entities.JpaVotingIssuerAccount;
import data.repositories.VotingRepository;
import devote.blockchain.api.DistributionAndBallotAccount;
import devote.blockchain.api.KeyPair;
import requests.CreateVotingRequest;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static data.repositories.imp.EbeanRepositoryUtils.assertEntityExists;
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
        assertEntityExists(ebeanServer, JpaVoting.class, id);
        return ebeanServer.find(JpaVoting.class, id);
    }

    @Override
    public void issuerAccountsCreated(Long id, List<KeyPair> keyPairs) {
        logger.info("issuerAccountsCreated(): id = {}, accounts size = {}", id, keyPairs.size());

        JpaVoting voting = ebeanServer.find(JpaVoting.class, id);

        List<JpaVotingIssuerAccount> votingIssuers = keyPairs.stream()
                .map(this::fromIssuerKeyPair)
                .collect(Collectors.toList());

        voting.setIssuerAccounts(votingIssuers);
        ebeanServer.merge(voting);
    }

    @Override
    public void channelAccountCreated(Long id, List<KeyPair> keyPairs) {
        logger.info("channelAccountCreated(): id = {}, accounts size = {}", id, keyPairs.size());

        JpaVoting voting = ebeanServer.find(JpaVoting.class, id);

        List<JpaVotingChannelAccount> channelAccounts = keyPairs.stream()
                .map(this::fromChannelKeyPair)
                .collect(Collectors.toList());

        voting.setChannelAccounts(channelAccounts);
        ebeanServer.merge(voting);
    }

    @Override
    public void distributionAndBallotAccountsCreated(Long id, DistributionAndBallotAccount.TransactionResult transactionResult) {
        logger.info("distributionAndBallotAccountsCreated(): id = {}", id);

        JpaVoting voting = ebeanServer.find(JpaVoting.class, id);
        voting.setDistributionAccountSecret(transactionResult.distribution.secretKey);
        voting.setDistributionAccountPublic(transactionResult.distribution.publicKey);
        voting.setBallotAccountSecret(transactionResult.ballot.secretKey);
        voting.setBallotAccountPublic(transactionResult.ballot.publicKey);

        ebeanServer.update(voting);

        Map<String, JpaVotingIssuerAccount> votingIssuersBySecret = new HashMap<>();
        voting.getIssuerAccounts().forEach(i -> votingIssuersBySecret.put(i.getAccountSecret(), i));

        transactionResult.issuerTokens.forEach((s, t) -> {
            JpaVotingIssuerAccount votingIssuer = votingIssuersBySecret.get(s);
            votingIssuer.setAssetCode(t);
            ebeanServer.update(votingIssuer);
        });
    }

    @Override
    public void votingSavedToIpfs(Long id, String ipfsCid) {
        logger.info("id = {}, ipfsCid = {}", id, ipfsCid);

        JpaVoting voting = ebeanServer.find(JpaVoting.class, id);
        voting.setIpfsCid(ipfsCid);
        ebeanServer.update(voting);
    }

    private JpaVotingIssuerAccount fromIssuerKeyPair(KeyPair keyPair) {
        JpaVotingIssuerAccount votingIssuer = new JpaVotingIssuerAccount();
        votingIssuer.setAccountSecret(keyPair.secretKey);
        votingIssuer.setAccountPublic(keyPair.publicKey);
        return votingIssuer;
    }

    private JpaVotingChannelAccount fromChannelKeyPair(KeyPair keyPair) {
        JpaVotingChannelAccount votingChannelAccount = new JpaVotingChannelAccount();
        votingChannelAccount.setAccountSecret(keyPair.secretKey);
        votingChannelAccount.setAccountPublic(keyPair.publicKey);
        return votingChannelAccount;
    }
}
