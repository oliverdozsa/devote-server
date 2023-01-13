package tasks.emailinvites;

import play.Logger;

public class EmailInvitesTask implements Runnable{
    private static final Logger.ALogger logger = Logger.of(EmailInvitesTask.class);

    private final EmailInvitesTaskContext context;

    public EmailInvitesTask(EmailInvitesTaskContext context) {
        this.context = context;
    }

    @Override
    public void run() {
        // TODO
    }
}
