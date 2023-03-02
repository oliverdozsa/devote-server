package data.repositories.imp;

import data.entities.*;
import data.repositories.VotingRepository;
import devote.blockchain.api.ChannelGenerator;
import devote.blockchain.api.DistributionAndBallotAccountOperation;
import devote.blockchain.api.Account;
import io.ebean.EbeanServer;
import play.Logger;
import requests.CreateVotingRequest;
import utils.StringUtils;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static data.repositories.imp.EbeanRepositoryUtils.assertEntityExists;
import static data.repositories.imp.EbeanVotingInit.initVotingFrom;

public class EbeanVotingRepository implements VotingRepository {
    private static final Logger.ALogger logger = Logger.of(EbeanVotingRepository.class);

    private final EbeanServer ebeanServer;

    @Inject
    public EbeanVotingRepository(EbeanServer ebeanServer) {
        this.ebeanServer = ebeanServer;
    }

    @Override
    public Long initialize(CreateVotingRequest request, String assetCode, String userId) {
        logger.info("initialize(): assetCode = {}, userId = {}, request = {}", assetCode, userId, request);

        JpaVoting voting = initVotingFrom(request);
        voting.setAssetCode(assetCode);
        voting.setCreatedBy(userId);

        checkForExistingUsers(voting);

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
    public void channelGeneratorsCreated(Long id, List<ChannelGenerator> channelGenerators) {
        logger.info("channelGeneratorsCreated(): id = {}, accounts size = {}", id, channelGenerators.size());

        JpaVoting voting = ebeanServer.find(JpaVoting.class, id);

        List<JpaChannelGeneratorAccount> jpaChannelGenerators = channelGenerators.stream()
                .map(this::fromChannelGenerator)
                .collect(Collectors.toList());

        voting.setChannelGeneratorAccounts(jpaChannelGenerators);
        ebeanServer.merge(voting);
    }

    @Override
    public void channelAccountCreated(Long id, List<Account> accounts) {
        logger.info("channelAccountCreated(): id = {}, accounts size = {}", id, accounts.size());

        JpaVoting voting = ebeanServer.find(JpaVoting.class, id);

        List<JpaVotingChannelAccount> channelAccounts = accounts.stream()
                .map(this::fromChannelKeyPair)
                .collect(Collectors.toList());

        voting.setChannelAccounts(channelAccounts);
        ebeanServer.merge(voting);
    }

    @Override
    public void distributionAndBallotAccountsCreated(Long id, DistributionAndBallotAccountOperation.TransactionResult transactionResult) {
        logger.info("distributionAndBallotAccountsCreated(): id = {}", id);

        JpaVoting voting = ebeanServer.find(JpaVoting.class, id);
        voting.setDistributionAccountSecret(transactionResult.distribution.secret);
        voting.setDistributionAccountPublic(transactionResult.distribution.publik);
        voting.setBallotAccountSecret(transactionResult.ballot.secret);
        voting.setBallotAccountPublic(transactionResult.ballot.publik);
        voting.setIssuerAccountPublic(transactionResult.issuer.publik);

        ebeanServer.update(voting);
    }

    @Override
    public void votingSavedToIpfs(Long id, String ipfsCid) {
        logger.info("votingSavedToIpfs(): id = {}, ipfsCid = {}", id, ipfsCid);

        JpaVoting voting = ebeanServer.find(JpaVoting.class, id);
        voting.setIpfsCid(ipfsCid);
        ebeanServer.update(voting);
    }

    @Override
    public List<JpaVoting> notInitializedSampleOf(int size) {
        logger.debug("notInitializedSampleOf(): size = {}", size);

        return ebeanServer.createQuery(JpaVoting.class)
                .where()
                .isNull("ipfsCid")
                .setMaxRows(size)
                .findList();
    }

    @Override
    public Optional<JpaVoting> findANotRefundedEndedVoting() {
        logger.debug("findAnEndedVotingWithNotRefundedDistribution()");

        return ebeanServer.createQuery(JpaVoting.class)
                .where()
                .eq("isInternalFundingRefunded", false)
                .lt("endDate", Instant.now())
                .setMaxRows(1)
                .findOneOrEmpty();
    }

    @Override
    public void distributionAccountRefunded(Long votingId) {
        logger.info("distributionAccountRefunded(): votingId = {}", votingId);

        JpaVoting voting = ebeanServer.find(JpaVoting.class, votingId);
        voting.setDistributionRefunded(true);
        ebeanServer.update(voting);
    }

    @Override
    public void internalFundingAccountCreated(Long id, Account funding) {
        logger.info("internalFundingAccountCreated(): id = {}, funding = {}", id, StringUtils.redactWithEllipsis(funding.publik, 5));

        JpaVoting voting = ebeanServer.find(JpaVoting.class, id);
        voting.setFundingAccountPublic(funding.publik);
        voting.setFundingAccountSecret(funding.secret);

        ebeanServer.update(voting);
    }

    @Override
    public void internalFundingAccountRefunded(Long id) {
        logger.info("internalFundingAccountRefunded(): id = {}", id);

        JpaVoting voting = ebeanServer.find(JpaVoting.class, id);
        voting.setInternalFundingRefunded(true);

        ebeanServer.update(voting);
    }

    @Override
    public Optional<JpaVoting> findOneWithAuthTokenNeedsToBeCreated() {
        logger.debug("findOneWithAuthTokenNeedsToBeCreated()");

        return ebeanServer.createQuery(JpaVoting.class)
                .where()
                .eq("isAuthTokenBased", true)
                .eq("isAuthTokensNeedToBeCreated", true)
                .findOneOrEmpty();
    }

    @Override
    public void allAuthTokensCreated(Long id) {
        logger.info("allAuthTokensCreated(): id = {}", id);

        JpaVoting voting = ebeanServer.find(JpaVoting.class, id);
        voting.setAuthTokensNeedToBeCreated(false);

        ebeanServer.save(voting);
    }

    private JpaChannelGeneratorAccount fromChannelGenerator(ChannelGenerator channelGenerator) {
        JpaChannelGeneratorAccount channelGeneratorEntity = new JpaChannelGeneratorAccount();
        channelGeneratorEntity.setAccountSecret(channelGenerator.account.secret);
        channelGeneratorEntity.setAccountPublic(channelGenerator.account.publik);
        channelGeneratorEntity.setVotesCap(channelGenerator.votesCap);
        return channelGeneratorEntity;
    }

    private JpaVotingChannelAccount fromChannelKeyPair(Account account) {
        JpaVotingChannelAccount votingChannelAccount = new JpaVotingChannelAccount();
        votingChannelAccount.setAccountSecret(account.secret);
        votingChannelAccount.setAccountPublic(account.publik);
        votingChannelAccount.setConsumed(false);
        return votingChannelAccount;
    }

    private void checkForExistingUsers(JpaVoting voting) {
        if (voting.getAuthorization() == Authorization.EMAILS) {
            this.checkForExistingUsersByEmail(voting);
        }
    }

    private void checkForExistingUsersByEmail(JpaVoting voting) {
        List<JpaVoter> checkedVoters = voting.getVoters().stream()
                .map(this::replaceWithExistingIfNeededByEmail)
                .collect(Collectors.toList());
        voting.setVoters(checkedVoters);
    }

    private JpaVoter replaceWithExistingIfNeededByEmail(JpaVoter voter) {
        JpaVoter existingVoter = ebeanServer.createQuery(JpaVoter.class)
                .where()
                .eq("email", voter.getEmail())
                .findOne();

        if (existingVoter == null) {
            return voter;
        } else {
            return existingVoter;
        }
    }
}
