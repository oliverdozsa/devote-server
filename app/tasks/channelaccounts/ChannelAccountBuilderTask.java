package tasks.channelaccounts;

import data.entities.JpaChannelAccountProgress;
import data.entities.JpaVotingIssuerAccount;
import devote.blockchain.BlockchainFactory;
import devote.blockchain.api.ChannelAccountFactory;
import devote.blockchain.api.KeyPair;
import play.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChannelAccountBuilderTask implements Runnable {
    private final int taskId;
    private final ChannelAccountBuilderTaskContext context;

    private static final Logger.ALogger logger = Logger.of(ChannelAccountBuilderTask.class);

    public ChannelAccountBuilderTask(int taskId, ChannelAccountBuilderTaskContext context) {
        this.taskId = taskId;
        this.context = context;

        logger.info("ChannelAccountBuilderTask(): created task with id = {}", taskId);
    }

    @Override
    public void run() {
        JpaChannelAccountProgress channelProgress = getAChannelAccountProgress();
        if (channelProgress == null) {
            logger.info("[CHANNEL-TASK-{}]: run(): No suitable channel progress found.", taskId);
            return;
        }

        List<KeyPair> channelAccountKeyPairs = createChannelAccounts(channelProgress);
        channelAccountsCreated(channelProgress, channelAccountKeyPairs);
    }

    private List<KeyPair> createChannelAccounts(JpaChannelAccountProgress channelProgress) {
        JpaVotingIssuerAccount issuer = channelProgress.getIssuer();
        ChannelAccountFactory channelAccountFactory = getChannelAccountFactory(issuer);

        long votesCap = issuer.getVoting().getVotesCap();
        int numOfAccountsToCreateInOneBatch =
                determineNumOfAccountsToCreateInOneBatch(channelProgress, channelAccountFactory);

        logger.info("[CHANNEL-TASK-{}]: createChannelAccounts(): about to create {} channel accounts on blockchain {} for progress {}",
                taskId, numOfAccountsToCreateInOneBatch, issuer.getVoting().getNetwork(), channelProgress.getId());

        List<KeyPair> channelAccountKeyPairs = new ArrayList<>();
        for (int i = 0; i < numOfAccountsToCreateInOneBatch; i++) {
            KeyPair issuerKeyPair = new KeyPair(issuer.getAccountSecret(), issuer.getAccountPublic());
            KeyPair accountKeyPair = channelAccountFactory.create(votesCap, issuerKeyPair);
            channelAccountKeyPairs.add(accountKeyPair);
        }

        logger.info("[CHANNEL-TASK-{}]: createChannelAccounts(): successfully created {} channel accounts on blockchain {}",
                taskId, channelAccountKeyPairs.size(), issuer.getVoting().getNetwork());

        return channelAccountKeyPairs;
    }

    private void channelAccountsCreated(JpaChannelAccountProgress channelProgress, List<KeyPair> channelKeyPairs) {
        Long votingId = channelProgress.getIssuer().getVoting().getId();

        context.votingRepository.channelAccountCreated(votingId, channelKeyPairs);
        context.channelProgressRepository.channelAccountsCreated(channelProgress.getId(), channelKeyPairs.size());
    }

    private ChannelAccountFactory getChannelAccountFactory(JpaVotingIssuerAccount issuer) {
        String network = issuer.getVoting().getNetwork();
        BlockchainFactory blockchainFactory = context.blockchains.getFactoryByNetwork(network);
        return blockchainFactory.createChannelAccount();
    }

    private JpaChannelAccountProgress getAChannelAccountProgress() {
        List<JpaChannelAccountProgress> sampleProgresses =
                context.channelProgressRepository.notFinishedSampleOf(context.voteBuckets);

        for (JpaChannelAccountProgress candidate : sampleProgresses) {
            if (candidate.getId() % context.voteBuckets == taskId) {
                return candidate;
            }
        }

        if (sampleProgresses.size() > 0) {
            List<Long> progressIds = sampleProgresses.stream()
                    .map(JpaChannelAccountProgress::getId)
                    .collect(Collectors.toList());

            logger.warn("[CHANNEL-TASK-{}]: getAChannelAccountProgress(): could not find a suitable channel progress! progress ids = {}",
                    taskId, progressIds);
        }

        return null;
    }

    private static int determineNumOfAccountsToCreateInOneBatch(JpaChannelAccountProgress progress, ChannelAccountFactory channelAccountFactory) {
        if (progress.getNumOfAccountsLeftToCreate() >= channelAccountFactory.maxNumOfAccountsToCreateInOneBatch()) {
            return channelAccountFactory.maxNumOfAccountsToCreateInOneBatch();
        } else {
            return progress.getNumOfAccountsLeftToCreate().intValue();
        }
    }
}
