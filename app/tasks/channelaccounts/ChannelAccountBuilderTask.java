package tasks.channelaccounts;

import data.entities.JpaChannelAccountProgress;
import data.entities.JpaChannelGeneratorAccount;
import devote.blockchain.BlockchainFactory;
import devote.blockchain.api.Account;
import devote.blockchain.api.ChannelAccountOperation;
import devote.blockchain.api.ChannelGenerator;
import play.Logger;

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

        List<Account> channelAccountAccounts = createChannelAccounts(channelProgress);
        channelAccountsCreated(channelProgress, channelAccountAccounts);
    }

    private List<Account> createChannelAccounts(JpaChannelAccountProgress channelProgress) {
        JpaChannelGeneratorAccount channelGeneratorEntity = channelProgress.getChannelGenerator();
        ChannelAccountOperation channelAccountOperation = getChannelAccountOperation(channelGeneratorEntity);
        if(channelProgress.getChannelGenerator().getVoting().getOnTestNetwork()) {
            channelAccountOperation.useTestNet();
        }

        int numOfAccountsToCreateInOneBatch = determineNumOfAccountsToCreateInOneBatch(channelProgress, channelAccountOperation);
        logger.info("[CHANNEL-TASK-{}]: createChannelAccounts(): about to create {} channel accounts on blockchain {} for progress {}",
                taskId, numOfAccountsToCreateInOneBatch, channelGeneratorEntity.getVoting().getNetwork(), channelProgress.getId());

        Account channelAccount = new Account(channelProgress.getChannelGenerator().getAccountSecret(), channelProgress.getChannelGenerator().getAccountPublic());
        ChannelGenerator channelGenerator = new ChannelGenerator(channelAccount, channelProgress.getNumOfAccountsToCreate());

        List<Account> createdAccounts = channelAccountOperation.create(channelGenerator, numOfAccountsToCreateInOneBatch);
        logger.info("[CHANNEL-TASK-{}]: createChannelAccounts(): successfully created {} channel accounts on blockchain {}",
                taskId, createdAccounts.size(), channelGeneratorEntity.getVoting().getNetwork());

        return createdAccounts;
    }

    private void channelAccountsCreated(JpaChannelAccountProgress channelProgress, List<Account> channelAccounts) {
        Long votingId = channelProgress.getChannelGenerator().getVoting().getId();

        context.votingRepository.channelAccountCreated(votingId, channelAccounts);
        context.channelProgressRepository.channelAccountsCreated(channelProgress.getId(), channelAccounts.size());
    }

    private ChannelAccountOperation getChannelAccountOperation(JpaChannelGeneratorAccount channelGeneratorAccount) {
        String network = channelGeneratorAccount.getVoting().getNetwork();
        BlockchainFactory blockchainFactory = context.blockchains.getFactoryByNetwork(network);
        return blockchainFactory.createChannelAccountOperation();
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

    private static int determineNumOfAccountsToCreateInOneBatch(JpaChannelAccountProgress progress, ChannelAccountOperation channelAccountOperation) {
        if (progress.getNumOfAccountsLeftToCreate() >= channelAccountOperation.maxNumOfAccountsToCreateInOneBatch()) {
            return channelAccountOperation.maxNumOfAccountsToCreateInOneBatch();
        } else {
            return progress.getNumOfAccountsLeftToCreate().intValue();
        }
    }
}
