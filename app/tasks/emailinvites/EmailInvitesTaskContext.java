package tasks.emailinvites;

import com.typesafe.config.Config;
import data.repositories.TokenAuthRepository;
import data.repositories.VoterRepository;
import data.repositories.VotingRepository;

import javax.inject.Inject;

public class EmailInvitesTaskContext {
    public final TokenAuthRepository tokenAuthRepository;
    public final VotingRepository votingRepository;
    public final VoterRepository voterRepository;
    public final int invitesToSendInOneBatch;

    @Inject
    public EmailInvitesTaskContext(TokenAuthRepository tokenAuthRepository, VotingRepository votingRepository, VoterRepository voterRepository, Config config) {
        this.tokenAuthRepository = tokenAuthRepository;
        this.votingRepository = votingRepository;
        this.voterRepository = voterRepository;
        invitesToSendInOneBatch = config.getInt("devote.email.invites.max.to.send.in.one.batch");
    }
}
