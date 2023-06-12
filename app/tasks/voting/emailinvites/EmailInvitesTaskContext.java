package tasks.voting.emailinvites;

import com.typesafe.config.Config;
import data.repositories.TokenAuthRepository;
import data.repositories.VoterRepository;
import data.repositories.VotingRepository;
import play.libs.mailer.MailerClient;

import javax.inject.Inject;

public class EmailInvitesTaskContext {
    public final TokenAuthRepository tokenAuthRepository;
    public final VotingRepository votingRepository;
    public final VoterRepository voterRepository;
    public final int invitesToSendInOneBatch;
    public final MailerClient mailerClient;

    @Inject
    public EmailInvitesTaskContext(TokenAuthRepository tokenAuthRepository, VotingRepository votingRepository,
                                   VoterRepository voterRepository, Config config, MailerClient mailerClient) {
        this.tokenAuthRepository = tokenAuthRepository;
        this.votingRepository = votingRepository;
        this.voterRepository = voterRepository;
        invitesToSendInOneBatch = config.getInt("galactic.host.vote.email.invites.max.to.send.in.one.batch");
        this.mailerClient = mailerClient;
    }
}
