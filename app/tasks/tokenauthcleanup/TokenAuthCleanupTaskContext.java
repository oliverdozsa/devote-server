package tasks.tokenauthcleanup;

import com.typesafe.config.Config;
import data.repositories.TokenAuthRepository;

import javax.inject.Inject;

public class TokenAuthCleanupTaskContext {
    public final TokenAuthRepository tokenAuthRepository;
    public final int maxToCleanupInOneBatch;
    public final int usableDaysAfterVotingEnded;

    @Inject
    public TokenAuthCleanupTaskContext(TokenAuthRepository tokenAuthRepository, Config config) {
        this.tokenAuthRepository = tokenAuthRepository;
        maxToCleanupInOneBatch = config.getInt("galactic.vote.tasks.token.auth.cleanup.max.in.one.batch");
        usableDaysAfterVotingEnded = config.getInt("galactic.vote.tasks.token.auth.usable.days.after.voting.ended");
    }
}

