package tasks.channelaccounts;

import data.entities.JpaChannelAccountProgress;
import data.entities.JpaVoting;
import data.entities.JpaVotingIssuer;
import data.repositories.ChannelProgressRepository;
import data.repositories.VotingRepository;
import devote.blockchain.BlockchainFactory;
import devote.blockchain.Blockchains;
import devote.blockchain.api.ChannelAccount;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

public class ChannelAccountBuilderTask implements Runnable {
    private final int id;
    private final Blockchains blockchains;
    private final VotingRepository votingRepository;
    private final ChannelProgressRepository channelProgressRepository;

    private static final Logger.ALogger logger = Logger.of(ChannelAccountBuilderTask.class);

    public ChannelAccountBuilderTask(
            int id,
            Blockchains blockchains,
            ChannelProgressRepository channelProgressRepository,
            VotingRepository votingRepository) {
        this.id = id;
        this.blockchains = blockchains;
        this.votingRepository = votingRepository;
        this.channelProgressRepository = channelProgressRepository;

        logger.info("ChannelAccountBuilderTask(): created task with id = {}", id);
    }

    @Override
    public void run() {
        logger.info("run(): executing task {}", id);

        JpaChannelAccountProgress channelProgress = getAChannelAccountProgress();
        if (channelProgress == null) {
            logger.info("[CHANNEL-TASK-{}]: run(): No channel progress found.", id);
            return;
        }

        List<String> channelAccountSecrets = createChannelAccounts(channelProgress);
        channelAccountsCreated(channelProgress, channelAccountSecrets);
    }

    private List<String> createChannelAccounts(JpaChannelAccountProgress channelProgress) {
        JpaVotingIssuer issuer = channelProgress.getIssuer();
        long votesCap = issuer.getVoting().getVotesCap();
        int numOfAccountsToCreateInOneBatch = determineAccountsToCreateInOneBatch();
        ChannelAccount channelAccount = getChannelAccount(issuer);

        List<String> channelAccountSecrets = new ArrayList<>();
        for (int i = 0; i < numOfAccountsToCreateInOneBatch; i++) {
            String accountSecret = channelAccount.create(votesCap, issuer.getAccountSecret());
            channelAccountSecrets.add(accountSecret);
        }

        return channelAccountSecrets;
    }

    private void channelAccountsCreated(JpaChannelAccountProgress channelProgress, List<String> channelSecrets) {
        Long votingId = channelProgress.getIssuer().getVoting().getId();

        votingRepository.channelAccountCreated(votingId, channelSecrets);
        channelProgressRepository.channelProgressRepository(channelProgress.getId(), channelSecrets.size());
    }

    private ChannelAccount getChannelAccount(JpaVotingIssuer issuer) {
        String network = issuer.getVoting().getNetwork();
        BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(network);
        return blockchainFactory.createChannelAccount();
    }

    private JpaChannelAccountProgress getAChannelAccountProgress() {
        // TODO
        return null;
    }

    private int determineAccountsToCreateInOneBatch() {
        // TODO
        return 0;
    }
}
