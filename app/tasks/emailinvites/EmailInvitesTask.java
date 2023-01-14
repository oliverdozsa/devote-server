package tasks.emailinvites;

import data.entities.JpaVoter;
import data.entities.JpaVoting;
import play.Logger;

import java.util.List;
import java.util.Optional;

public class EmailInvitesTask implements Runnable {
    private static final Logger.ALogger logger = Logger.of(EmailInvitesTask.class);

    private final EmailInvitesTaskContext context;

    public EmailInvitesTask(EmailInvitesTaskContext context) {
        this.context = context;

        logger.info("EmailInvitesTask(): created");
    }

    @Override
    public void run() {
        Optional<JpaVoting> optionalVoting = context.votingRepository.findOneWithAuthTokenNeedsToBeCreated();

        if (!optionalVoting.isPresent()) {
            logger.info("[EMAIL-INVITE-TASK]: Not found voting that has auth tokens left to create.");
            return;
        }

        JpaVoting voting = optionalVoting.get();

        List<JpaVoter> votersWhoNeedAuthTokens =
                context.voterRepository.findThoseWhoNeedsAuthToken(voting.getId(), context.invitesToSendInOneBatch);

        if(votersWhoNeedAuthTokens.size() == 0) {
            logger.info("[EMAIL-INVITE-TASK]: No more voters need auth tokens in voting: {}", voting.getId());
            context.votingRepository.allAuthTokensCreated(voting.getId());
            return;
        }

        logger.info("[EMAIL-INVITE-TASK]: Creating tokens for {} voters", votersWhoNeedAuthTokens.size());
        votersWhoNeedAuthTokens.forEach(voter -> context.tokenAuthRepository.createFor(voting.getId(), voter.getId()));

        // TODO: send the invites too. But of course only send really if it's in prod
    }
}
