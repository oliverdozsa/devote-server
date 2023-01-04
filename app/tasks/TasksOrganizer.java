package tasks;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import play.Logger;
import scala.concurrent.ExecutionContext;
import tasks.channelaccounts.ChannelAccountBuilderTask;
import tasks.channelaccounts.ChannelAccountBuilderTaskContext;
import tasks.refundbalances.RefundBalancesTask;
import tasks.refundbalances.RefundBalancesTaskContext;
import tasks.votingblockchaininit.VotingBlockchainInitTask;
import tasks.votingblockchaininit.VotingBlockchainInitTaskContext;

import javax.inject.Inject;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TasksOrganizer {
    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    private final int numberOfWorkers;
    private final ChannelAccountBuilderTaskContext channelContext;
    private final VotingBlockchainInitTaskContext votingInitContext;
    private final RefundBalancesTaskContext refundBalancesContext;

    private static final Logger.ALogger logger = Logger.of(TasksOrganizer.class);

    private final int initialDelaySecs;
    private final int channelTaskIntervalSecs;
    private final int votingInitTaskIntervalSecs;
    private final int refundBalancesIntervalSecs ;

    @Inject
    public TasksOrganizer(
            Config config,
            ActorSystem actorSystem,
            ExecutionContext executionContext,
            ChannelAccountBuilderTaskContext channelContext,
            VotingBlockchainInitTaskContext votingInitContext,
            RefundBalancesTaskContext refundBalancesContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.channelContext = channelContext;
        this.votingInitContext = votingInitContext;
        this.refundBalancesContext = refundBalancesContext;

        initialDelaySecs = config.getInt("devote.tasks.initial.delay.secs");
        channelTaskIntervalSecs = config.getInt("devote.tasks.channel.interval.secs");
        votingInitTaskIntervalSecs = config.getInt("devote.tasks.voting.init.interval.secs");
        refundBalancesIntervalSecs = config.getInt("devote.tasks.refund.balances.interval.secs");

        numberOfWorkers = config.getInt("devote.vote.buckets");

        initializeChannelBuilderTasks();
        initializeVotingInitTasks();
        initializeRefundBalancesTask();
    }

    private void initializeChannelBuilderTasks() {
        List<Runnable> channelTasks = new ArrayList<>();
        for (int i = 0; i < numberOfWorkers; i++) {
            channelTasks.add(new ChannelAccountBuilderTask(i, channelContext));
        }

        initialize(channelTasks, "channel builder", channelTaskIntervalSecs);
    }

    private void initializeVotingInitTasks() {
        List<Runnable> votingInitTasks = new ArrayList<>();
        for (int i = 0; i < numberOfWorkers; i++) {
            votingInitTasks.add(new VotingBlockchainInitTask(i, votingInitContext));
        }

        initialize(votingInitTasks, "voting init", votingInitTaskIntervalSecs);
    }

    private void initializeRefundBalancesTask() {
        RefundBalancesTask task = new RefundBalancesTask(refundBalancesContext);
        initialize(Collections.singletonList(task), "refund balances", refundBalancesIntervalSecs);
    }

    private void initialize(List<Runnable> tasks, String name, long intervalSecs) {
        logger.info("initialize(): creating {} workers for {}", tasks.size(), name);

        for (Runnable task : tasks) {
            this.actorSystem.scheduler()
                    .scheduleAtFixedRate(
                            Duration.ofSeconds(initialDelaySecs),
                            Duration.ofSeconds(intervalSecs),
                            task,
                            executionContext
                    );
        }
    }
}
