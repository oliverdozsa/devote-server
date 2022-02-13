package tasks.votinginit;

import data.entities.JpaVoting;
import play.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class VotingInitTask implements Runnable {
    private final int taskId;
    private final VotingInitTaskContext context;

    private static final Logger.ALogger logger = Logger.of(VotingInitTask.class);

    public VotingInitTask(int taskId, VotingInitTaskContext context) {
        this.taskId = taskId;
        this.context = context;

        logger.info("VotingInitTask(): created task with id = {}", taskId);
    }

    @Override
    public void run() {
        JpaVoting voting = getAVoting();
        if (voting == null) {
            logger.info("[VOTING-INIT-TASK-{}]: Could not find a voting to init.", taskId);
            return;
        }

        initializeOnBlockchain(voting);
    }

    private JpaVoting getAVoting() {
        List<JpaVoting> notInitializedVotings = context.votingRepository.notInitializedSampleOf(context.voteBuckets);

        for (JpaVoting candidate : notInitializedVotings) {
            if (candidate.getId() % context.voteBuckets == taskId) {
                return candidate;
            }
        }

        if (notInitializedVotings.size() > 0) {
            List<Long> notInitializedVotingIds = notInitializedVotings.stream()
                    .map(JpaVoting::getId)
                    .collect(Collectors.toList());
            logger.warn("[VOTING-INIT-TASK-{}]: Could not find suitable voting! voting ids in sample = {}", taskId, notInitializedVotingIds);
        }

        return null;
    }

    private void initializeOnBlockchain(JpaVoting voting) {
        // TODO
    }
}
