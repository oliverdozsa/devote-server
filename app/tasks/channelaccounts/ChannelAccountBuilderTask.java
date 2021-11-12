package tasks.channelaccounts;

import data.entities.JpaChannelAccountProgress;
import data.entities.JpaVotingIssuerAccount;
import devote.blockchain.BlockchainFactory;
import devote.blockchain.api.ChannelAccount;
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

        List<String> channelAccountSecrets = createChannelAccounts(channelProgress);
        channelAccountsCreated(channelProgress, channelAccountSecrets);
    }

    private List<String> createChannelAccounts(JpaChannelAccountProgress channelProgress) {
        JpaVotingIssuerAccount issuer = channelProgress.getIssuer();
        ChannelAccount channelAccount = getChannelAccount(issuer);

        long votesCap = issuer.getVoting().getVotesCap();
        int numOfAccountsToCreateInOneBatch =
                determineNumOfAccountsToCreateInOneBatch(channelProgress, channelAccount);

        logger.info("[CHANNEL-TASK-{}]: createChannelAccounts(): about to create {} channel accounts on blockchain for progress {}",
                taskId, numOfAccountsToCreateInOneBatch, channelProgress.getId());

        List<String> channelAccountSecrets = new ArrayList<>();
        for (int i = 0; i < numOfAccountsToCreateInOneBatch; i++) {
            String accountSecret = channelAccount.create(votesCap, issuer.getAccountSecret());
            channelAccountSecrets.add(accountSecret);
        }

        logger.info("[CHANNEL-TASK-{}]: createChannelAccounts(): successfully created {} channel accounts on blockchain",
                taskId, channelAccountSecrets.size());

        return channelAccountSecrets;
    }

    private void channelAccountsCreated(JpaChannelAccountProgress channelProgress, List<String> channelSecrets) {
        Long votingId = channelProgress.getIssuer().getVoting().getId();

        context.votingRepository.channelAccountCreated(votingId, channelSecrets);
        context.channelProgressRepository.channelAccountsCreated(channelProgress.getId(), channelSecrets.size());
    }

    private ChannelAccount getChannelAccount(JpaVotingIssuerAccount issuer) {
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

    private static int determineNumOfAccountsToCreateInOneBatch(JpaChannelAccountProgress progress, ChannelAccount channelAccount) {
        if (progress.getNumOfAccountsToLeftToCreate() >= channelAccount.maxNumOfAccountsToCreateInOneBatch()) {
            return channelAccount.maxNumOfAccountsToCreateInOneBatch();
        } else {
            return progress.getNumOfAccountsToLeftToCreate().intValue();
        }
    }
}
