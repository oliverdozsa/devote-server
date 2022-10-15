package tasks.votingblockchaininit;

import com.fasterxml.jackson.databind.JsonNode;
import data.entities.JpaVoting;
import devote.blockchain.BlockchainFactory;
import devote.blockchain.api.Account;
import devote.blockchain.api.ChannelGenerator;
import devote.blockchain.api.ChannelGeneratorAccountOperation;
import devote.blockchain.api.DistributionAndBallotAccountOperation;
import ipfs.data.IpfsVoting;
import ipfs.data.IpfsVotingFromJpaVoting;
import play.Logger;
import play.libs.Json;

import java.util.List;
import java.util.stream.Collectors;

public class VotingBlockchainInitTask implements Runnable {
    private final int taskId;
    private final VotingBlockchainInitTaskContext context;
    private final IpfsVotingFromJpaVoting ipfsVotingFromJpaVoting;

    private static final Logger.ALogger logger = Logger.of(VotingBlockchainInitTask.class);

    public VotingBlockchainInitTask(int taskId, VotingBlockchainInitTaskContext context) {
        this.taskId = taskId;
        this.context = context;
        ipfsVotingFromJpaVoting = new IpfsVotingFromJpaVoting();

        logger.info("VotingInitTask(): created task with id = {}", taskId);
    }

    @Override
    public void run() {
        JpaVoting voting = getANotFullyInitializedVoting();
        if (voting == null) {
            logger.info("[VOTING-BC-INIT-TASK-{}]: Could not find a voting to init.", taskId);
            return;
        }

        initializeOnBlockchain(voting);
    }

    private JpaVoting getANotFullyInitializedVoting() {
        List<JpaVoting> notInitializedVotings = context.votingRepository.notInitializedSampleOf(context.voteBuckets);

        for (JpaVoting candidate : notInitializedVotings) {
            if (candidate.getId() % context.voteBuckets == taskId) {
                return candidate;
            }
        }

        if (notInitializedVotings.size() > 0) {
            List<Long> notInitializedVotingIds = notInitializedVotings.stream()
                    .map(JpaVoting::getId)
                    .collect(Collectors.toList());
            logger.warn("[VOTING-BC-INIT-TASK-{}]: Could not find suitable voting for this task " +
                    "(should be handled by other tasks)! voting ids in sample = {}", taskId, notInitializedVotingIds);
        }

        return null;
    }

    private void initializeOnBlockchain(JpaVoting voting) {
        createChannelGeneratorAccountsIfNeeded(voting);
        createDistributionAndBallotAccountsIfNeeded(voting);

        // This should be the last step (used for checking whether voting is fully initialized, or not).
        saveVotingToIpfs(voting);
    }

    private void createChannelGeneratorAccountsIfNeeded(JpaVoting voting) {
        if (voting.getChannelGeneratorAccounts() != null && voting.getChannelGeneratorAccounts().size() > 0) {
            logger.info("[VOTING-BC-INIT-TASK-{}]: channel generators have already been created for voting {}", taskId, voting.getId());
            return;
        }

        logger.info("[VOTING-BC-INIT-TASK-{}]: creating channel generators for voting {}", taskId, voting.getId());

        ChannelGeneratorAccountOperation channelGeneratorAccountOperation = getChannelGeneratorOperation(voting.getNetwork());
        if (voting.getOnTestNetwork()) {
            channelGeneratorAccountOperation.useTestNet();
        }

        List<ChannelGenerator> channelGenerators = channelGeneratorAccountOperation.create(voting.getVotesCap(), getFundingOf(voting));
        context.votingRepository.channelGeneratorsCreated(voting.getId(), channelGenerators);
        context.channelProgressRepository.channelGeneratorsCreated(voting.getId());
    }

    private void createDistributionAndBallotAccountsIfNeeded(JpaVoting voting) {
        if (voting.getDistributionAccountPublic() != null && voting.getDistributionAccountPublic().length() > 0) {
            logger.info("[VOTING-BC-INIT-TASK-{}]: ballot and distribution accounts have already been created for voting {}", taskId, voting.getId());
            return;
        }

        logger.info("[VOTING-BC-INIT-TASK-{}]: creating ballot and distribution generators for voting {}", taskId, voting.getId());

        DistributionAndBallotAccountOperation distributionAndBallotAccountOperation = getDistributionAndBallotOperation(voting.getNetwork());
        if (voting.getOnTestNetwork()) {
            distributionAndBallotAccountOperation.useTestNet();
        }

        DistributionAndBallotAccountOperation.TransactionResult transactionResult =
                distributionAndBallotAccountOperation.create(getFundingOf(voting), voting.getAssetCode(), voting.getVotesCap());

        context.votingRepository.distributionAndBallotAccountsCreated(voting.getId(), transactionResult);
    }

    private void saveVotingToIpfs(JpaVoting voting) {
        // Ipfs voting is used for checking whether voting is initialized, or not, so we don't check
        // for existence of voting in IPFS.

        logger.info("[VOTING-BC-INIT-TASK-{}]: Saving voting {} to ipfs.", taskId, voting.getId());
        IpfsVoting ipfsVoting = ipfsVotingFromJpaVoting.convert(voting);
        JsonNode ipfsVotingJson = Json.toJson(ipfsVoting);
        String cid = context.ipfsApi.saveJson(ipfsVotingJson);
        context.votingRepository.votingSavedToIpfs(voting.getId(), cid);
    }

    private ChannelGeneratorAccountOperation getChannelGeneratorOperation(String network) {
        BlockchainFactory blockchainFactory = context.blockchains.getFactoryByNetwork(network);
        return blockchainFactory.createChannelGeneratorAccountOperation();
    }

    private Account getFundingOf(JpaVoting voting) {
        return new Account(voting.getFundingAccountSecret(), voting.getFundingAccountPublic());
    }

    private DistributionAndBallotAccountOperation getDistributionAndBallotOperation(String network) {
        BlockchainFactory blockchainFactory = context.blockchains.getFactoryByNetwork(network);
        return blockchainFactory.createDistributionAndBallotAccountOperation();
    }
}
