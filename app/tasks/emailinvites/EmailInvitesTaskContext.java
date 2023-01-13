package tasks.emailinvites;

import data.repositories.TokenAuthRepository;

import javax.inject.Inject;

public class EmailInvitesTaskContext {
    public final TokenAuthRepository tokenAuthRepository;

    @Inject
    public EmailInvitesTaskContext(TokenAuthRepository tokenAuthRepository) {
        this.tokenAuthRepository = tokenAuthRepository;
    }
}
