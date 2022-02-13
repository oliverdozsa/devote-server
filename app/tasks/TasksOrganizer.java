package tasks;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import play.Logger;
import scala.concurrent.ExecutionContext;
import tasks.channelaccounts.ChannelAccountBuilderTask;
import tasks.channelaccounts.ChannelAccountBuilderTaskContext;
import tasks.votinginit.VotingInitTask;
import tasks.votinginit.VotingInitTaskContext;

import javax.inject.Inject;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TasksOrganizer {
    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    private final int numberOfWorkers;
    private final ChannelAccountBuilderTaskContext channelContext;
    private final VotingInitTaskContext votingInitContext;

    private static final Logger.ALogger logger = Logger.of(TasksOrganizer.class);

    private static final int INITIAL_DELAY_SEC = 5;
    private static final int CHANNEL_TASK_INTERVAL_SEC = 7;
    private static final int VOTING_INIT_TASK_INTERVAL_SEC = 11;

    @Inject
    public TasksOrganizer(
            Config config,
            ActorSystem actorSystem,
            ExecutionContext executionContext,
            ChannelAccountBuilderTaskContext channelContext,
            VotingInitTaskContext votingInitContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.channelContext = channelContext;
        this.votingInitContext = votingInitContext;
        numberOfWorkers = config.getInt("devote.vote.buckets");

        initializeChannelBuilderTasks();
        initializeVotingInitTasks();
    }

    private void initializeChannelBuilderTasks() {
        List<Runnable> channelTasks = new ArrayList<>();
        for (int i = 0; i < numberOfWorkers; i++) {
            channelTasks.add(new ChannelAccountBuilderTask(i, channelContext));
        }

        initialize(channelTasks, "channel builder", CHANNEL_TASK_INTERVAL_SEC);
    }

    private void initializeVotingInitTasks() {
        List<Runnable> votingInitTasks = new ArrayList<>();
        for (int i = 0; i < numberOfWorkers; i++) {
            votingInitTasks.add(new VotingInitTask(i, votingInitContext));
        }

        initialize(votingInitTasks, "voting init", VOTING_INIT_TASK_INTERVAL_SEC);
    }

    private void initialize(List<Runnable> tasks, String name, long intervalSecs) {
        logger.info("initialize(): creating {} workers for {}", tasks.size(), name);

        for (Runnable task : tasks) {
            this.actorSystem.scheduler()
                    .scheduleAtFixedRate(
                            Duration.ofSeconds(INITIAL_DELAY_SEC),
                            Duration.ofSeconds(intervalSecs),
                            task,
                            executionContext
                    );
        }
    }
}
