package tasks.channelaccounts;

import data.entities.JpaChannelAccountProgress;
import data.entities.JpaVotingIssuerAccount;
import devote.blockchain.BlockchainFactory;
import devote.blockchain.api.ChannelAccountOperation;
import devote.blockchain.api.Account;
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

        List<Account> channelAccountAccounts = createChannelAccounts(channelProgress);
        channelAccountsCreated(channelProgress, channelAccountAccounts);
    }

    private List<Account> createChannelAccounts(JpaChannelAccountProgress channelProgress) {
        JpaVotingIssuerAccount issuer = channelProgress.getIssuer();
        ChannelAccountOperation channelAccountOperation = getChannelAccountFactory(issuer);

        long votesCap = issuer.getVoting().getVotesCap();
        int numOfAccountsToCreateInOneBatch =
                determineNumOfAccountsToCreateInOneBatch(channelProgress, channelAccountOperation);

        logger.info("[CHANNEL-TASK-{}]: createChannelAccounts(): about to create {} channel accounts on blockchain {} for progress {}",
                taskId, numOfAccountsToCreateInOneBatch, issuer.getVoting().getNetwork(), channelProgress.getId());

        List<Account> channelAccountAccounts = new ArrayList<>();
        for (int i = 0; i < numOfAccountsToCreateInOneBatch; i++) {
            Account issuerAccount = new Account(issuer.getAccountSecret(), issuer.getAccountPublic());
            Account accountAccount = channelAccountOperation.create(votesCap, issuerAccount);
            channelAccountAccounts.add(accountAccount);
        }

        logger.info("[CHANNEL-TASK-{}]: createChannelAccounts(): successfully created {} channel accounts on blockchain {}",
                taskId, channelAccountAccounts.size(), issuer.getVoting().getNetwork());

        return channelAccountAccounts;
    }

    private void channelAccountsCreated(JpaChannelAccountProgress channelProgress, List<Account> channelAccounts) {
        Long votingId = channelProgress.getIssuer().getVoting().getId();

        context.votingRepository.channelAccountCreated(votingId, channelAccounts);
        context.channelProgressRepository.channelAccountsCreated(channelProgress.getId(), channelAccounts.size());
    }

    private ChannelAccountOperation getChannelAccountFactory(JpaVotingIssuerAccount issuer) {
        String network = issuer.getVoting().getNetwork();
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
