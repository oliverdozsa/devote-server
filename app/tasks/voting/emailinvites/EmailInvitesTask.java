package tasks.voting.emailinvites;

import data.entities.voting.JpaAuthToken;
import data.entities.voting.JpaVoter;
import data.entities.voting.JpaVoting;
import play.Logger;
import play.libs.mailer.Email;

import java.util.List;
import java.util.Optional;

public class EmailInvitesTask implements Runnable {
    private static final Logger.ALogger logger = Logger.of(EmailInvitesTask.class);

    private final EmailInvitesTaskContext context;

    private static final String BODY_TEMPLATE = "Hello!\n" +
            "You've been invited to participate in voting: \"%s\", organized by \"%s\".\n\n" +
            "Here's the link, which gives you access to the voting: %s.\n" +
            "Please keep this link private; never share it with anyone.";

    public EmailInvitesTask(EmailInvitesTaskContext context) {
        this.context = context;

        logger.info("EmailInvitesTask(): created");
    }

    @Override
    public void run() {
        Optional<JpaVoting> optionalVoting = context.votingRepository.findOneWithAuthTokenNeedsToBeCreated();

        if (!optionalVoting.isPresent()) {
            logger.debug("[EMAIL-INVITE-TASK]: Not found voting that has auth tokens left to create.");
            return;
        }

        JpaVoting voting = optionalVoting.get();

        List<JpaVoter> votersWhoNeedAuthTokens =
                context.voterRepository.findThoseWhoNeedsAuthToken(voting.getId(), context.invitesToSendInOneBatch);

        if (votersWhoNeedAuthTokens.size() == 0) {
            logger.debug("[EMAIL-INVITE-TASK]: No more voters need auth tokens in voting: {}", voting.getId());
            context.votingRepository.allAuthTokensCreated(voting.getId());
            return;
        }

        logger.info("[EMAIL-INVITE-TASK]: Creating tokens and sending emails for {} voters", votersWhoNeedAuthTokens.size());
        votersWhoNeedAuthTokens.forEach(voter -> {
            JpaAuthToken jpaAuthToken = context.tokenAuthRepository.createFor(voting.getId(), voter.getId());
            sendInviteMailFor(jpaAuthToken);
        });
    }

    private void sendInviteMailFor(JpaAuthToken jpaAuthToken) {
        String link = "https://galactic.pub/voting-invite/" + jpaAuthToken.getToken().toString();

        String bodyText = String.format(BODY_TEMPLATE,
                jpaAuthToken.getVoting().getTitle(),
                jpaAuthToken.getVoting().getOrganizer(),
                link);

        Email email = new Email()
                .setSubject("You've been invited to participate in voting: \"" + jpaAuthToken.getVoting().getTitle() + "\"")
                .setFrom("Galactic Vote Invite Bot <info@galactic.pub>")
                .addTo("<" + jpaAuthToken.getVoter().getEmail() + ">")
                .setBodyText(bodyText);

        context.mailerClient.send(email);
    }
}
