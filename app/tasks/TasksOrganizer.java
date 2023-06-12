package tasks;

import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import com.typesafe.config.Config;
import play.Logger;
import play.api.inject.ApplicationLifecycle;
import scala.concurrent.ExecutionContext;
import tasks.voting.channelaccounts.ChannelAccountBuilderTask;
import tasks.voting.channelaccounts.ChannelAccountBuilderTaskContext;
import tasks.voting.emailinvites.EmailInvitesTask;
import tasks.voting.emailinvites.EmailInvitesTaskContext;
import tasks.voting.refundbalances.RefundBalancesTask;
import tasks.voting.refundbalances.RefundBalancesTaskContext;
import tasks.voting.tokenauthcleanup.TokenAuthCleanupTask;
import tasks.voting.tokenauthcleanup.TokenAuthCleanupTaskContext;
import tasks.voting.votingblockchaininit.VotingBlockchainInitTask;
import tasks.voting.votingblockchaininit.VotingBlockchainInitTaskContext;

import javax.inject.Inject;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class TasksOrganizer {
    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    private final int numberOfWorkers;
    private final ChannelAccountBuilderTaskContext channelContext;
    private final VotingBlockchainInitTaskContext votingInitContext;
    private final RefundBalancesTaskContext refundBalancesContext;
    private final EmailInvitesTaskContext emailInvitesTaskContext;
    private final TokenAuthCleanupTaskContext tokenAuthCleanupTaskContext;

    private static final Logger.ALogger logger = Logger.of(TasksOrganizer.class);

    private final int initialDelayMillis;
    private final int channelTaskIntervalMillis;
    private final int votingInitTaskIntervalMillis;
    private final int refundBalancesTaskIntervalMillis;
    private final int emailInvitesTaskIntervalMillis;
    private final int tokenAuthCleanupTaskIntervalMillis;

    private final List<Cancellable> taskCancellables = new ArrayList<>();

    @Inject
    public TasksOrganizer(
            Config config,
            ActorSystem actorSystem,
            ExecutionContext executionContext,
            ChannelAccountBuilderTaskContext channelContext,
            VotingBlockchainInitTaskContext votingInitContext,
            RefundBalancesTaskContext refundBalancesContext,
            EmailInvitesTaskContext emailInvitesTaskContext,
            TokenAuthCleanupTaskContext tokenAuthCleanupTaskContext,
            ApplicationLifecycle lifecycle) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.channelContext = channelContext;
        this.votingInitContext = votingInitContext;
        this.refundBalancesContext = refundBalancesContext;
        this.emailInvitesTaskContext = emailInvitesTaskContext;
        this.tokenAuthCleanupTaskContext = tokenAuthCleanupTaskContext;

        initialDelayMillis = config.getInt("galactic.host.tasks.initial.delay.millis");
        channelTaskIntervalMillis = config.getInt("galactic.host.vote.tasks.channel.interval.millis");
        votingInitTaskIntervalMillis = config.getInt("galactic.host.vote.tasks.voting.init.interval.millis");
        refundBalancesTaskIntervalMillis = config.getInt("galactic.host.vote.tasks.refund.balances.interval.millis");
        emailInvitesTaskIntervalMillis = config.getInt("galactic.host.vote.tasks.email.invites.interval.millis");
        tokenAuthCleanupTaskIntervalMillis = config.getInt("galactic.host.vote.tasks.token.auth.cleanup.interval.millis");

        numberOfWorkers = config.getInt("galactic.host.vote.vote.buckets");

        initializeChannelBuilderTasks();
        initializeVotingInitTasks();
        initializeRefundBalancesTask();
        initializeEmailInvitesTask();
        initializeTokenAuthCleanupTask();

        lifecycle.addStopHook(() -> {
            taskCancellables.forEach(Cancellable::cancel);
            return completedFuture(null);
        });
    }

    private void initializeChannelBuilderTasks() {
        List<Runnable> channelTasks = new ArrayList<>();
        for (int i = 0; i < numberOfWorkers; i++) {
            channelTasks.add(new ChannelAccountBuilderTask(i, channelContext));
        }

        initialize(channelTasks, "channel builder", channelTaskIntervalMillis);
    }

    private void initializeVotingInitTasks() {
        List<Runnable> votingInitTasks = new ArrayList<>();
        for (int i = 0; i < numberOfWorkers; i++) {
            votingInitTasks.add(new VotingBlockchainInitTask(i, votingInitContext));
        }

        initialize(votingInitTasks, "voting init", votingInitTaskIntervalMillis);
    }

    private void initializeRefundBalancesTask() {
        RefundBalancesTask task = new RefundBalancesTask(refundBalancesContext);
        initialize(Collections.singletonList(task), "refund balances", refundBalancesTaskIntervalMillis);
    }

    private void initializeEmailInvitesTask() {
        EmailInvitesTask task = new EmailInvitesTask(emailInvitesTaskContext);
        initialize(Collections.singletonList(task), "email invites", emailInvitesTaskIntervalMillis);
    }

    private void initializeTokenAuthCleanupTask() {
        TokenAuthCleanupTask task = new TokenAuthCleanupTask(tokenAuthCleanupTaskContext);
        initialize(Collections.singletonList(task), "token auth cleanup", tokenAuthCleanupTaskIntervalMillis);
    }

    private void initialize(List<Runnable> tasks, String name, long intervalMillis) {
        logger.info("initialize(): creating {} workers for {}", tasks.size(), name);

        for (Runnable task : tasks) {
            Cancellable cancellable = this.actorSystem.scheduler()
                    .scheduleAtFixedRate(
                            Duration.ofMillis(initialDelayMillis),
                            Duration.ofMillis(intervalMillis),
                            task,
                            executionContext
                    );
            taskCancellables.add(cancellable);
        }
    }
}
