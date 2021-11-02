package tasks.channelaccounts;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import play.Logger;
import scala.concurrent.ExecutionContext;

import javax.inject.Inject;
import java.time.Duration;

public class ChannelAccountTasksOrganizer {
    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    private final int numberOfWorkers;
    private final ChannelAccountBuilderTaskContext context;

    private static final Logger.ALogger logger = Logger.of(ChannelAccountTasksOrganizer.class);

    private static final int INITIAL_DELAY_SEC = 5;
    private static final int INTERVAL_SEC = 7;

    @Inject
    public ChannelAccountTasksOrganizer(
            Config config,
            ActorSystem actorSystem,
            ExecutionContext executionContext,
            ChannelAccountBuilderTaskContext context) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.context = context;

        numberOfWorkers = config.getInt("devote.vote.buckets");

        initialize();
    }

    private void initialize() {
        logger.info("initialize(): creating {} workers", numberOfWorkers);

        for (int i = 0; i < numberOfWorkers; i++) {
            this.actorSystem.scheduler()
                    .scheduleAtFixedRate(
                            Duration.ofSeconds(INITIAL_DELAY_SEC),
                            Duration.ofSeconds(INTERVAL_SEC),
                            new ChannelAccountBuilderTask(i, context),
                            executionContext
                    );
        }
    }
}
