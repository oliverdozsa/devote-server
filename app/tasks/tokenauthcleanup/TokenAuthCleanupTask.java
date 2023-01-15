package tasks.tokenauthcleanup;

import play.Logger;

public class TokenAuthCleanupTask implements Runnable {
    private static final Logger.ALogger logger = Logger.of(TokenAuthCleanupTask.class);

    private final TokenAuthCleanupTaskContext context;

    public TokenAuthCleanupTask(TokenAuthCleanupTaskContext context) {
        this.context = context;
        logger.info("[TOKEN-AUTH-CLEANUP]: created()");
    }

    @Override
    public void run() {
        logger.info("[TOKEN-AUTH-CLEANUP]: Cleaning up max {} expired auth tokens older than {} days after voting ended",
                context.maxToCleanupInOneBatch, context.usableDaysAfterVotingEnded);
        context.tokenAuthRepository.cleanupOlderThanFromVotingEndDate(context.usableDaysAfterVotingEnded, context.maxToCleanupInOneBatch);
    }
}
