package tasks.voting.channelaccounts;

import data.entities.JpaChannelAccountProgress;
import data.entities.JpaChannelGeneratorAccount;
import galactic.blockchain.BlockchainFactory;
import galactic.blockchain.api.Account;
import galactic.blockchain.api.ChannelAccountOperation;
import galactic.blockchain.api.ChannelGenerator;
import play.Logger;

import java.util.List;

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
        try {
            JpaChannelAccountProgress channelProgress = getAChannelAccountProgress();
            if (channelProgress == null) {
                logger.debug("[CHANNEL-TASK-{}]: No suitable channel progress found.", taskId);
                return;
            }

            List<Account> channelAccountAccounts = createChannelAccounts(channelProgress);
            channelAccountsCreated(channelProgress, channelAccountAccounts);
        } catch (Exception e) {
            logger.warn("[CHANNEL-TASK-{}]: Exception during channel accounts creation (will retry).:\n{}", taskId, e);
        }

    }

    private List<Account> createChannelAccounts(JpaChannelAccountProgress channelProgress) {
        JpaChannelGeneratorAccount channelGeneratorEntity = channelProgress.getChannelGenerator();
        ChannelAccountOperation channelAccountOperation = getChannelAccountOperation(channelGeneratorEntity);
        if (channelProgress.getChannelGenerator().getVoting().getOnTestNetwork()) {
            channelAccountOperation.useTestNet();
        }

        int numOfAccountsToCreateInOneBatch = determineNumOfAccountsToCreateInOneBatch(channelProgress, channelAccountOperation);
        logger.info("[CHANNEL-TASK-{}]: about to create {} channel accounts on blockchain {} for progress {}",
                taskId, numOfAccountsToCreateInOneBatch, channelGeneratorEntity.getVoting().getNetwork(), channelProgress.getId());

        Account channelAccount = new Account(channelProgress.getChannelGenerator().getAccountSecret(), channelProgress.getChannelGenerator().getAccountPublic());
        ChannelGenerator channelGenerator = new ChannelGenerator(channelAccount, channelProgress.getNumOfAccountsToCreate());

        List<Account> createdAccounts = channelAccountOperation.create(channelGenerator, numOfAccountsToCreateInOneBatch);
        logger.info("[CHANNEL-TASK-{}]: successfully created {} channel accounts on blockchain {}",
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
