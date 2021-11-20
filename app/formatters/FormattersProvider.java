package formatters;

import play.data.format.Formatters;
import play.i18n.MessagesApi;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;

public class FormattersProvider implements Provider<Formatters> {
    private final MessagesApi messagesApi;

    @Inject
    public FormattersProvider(MessagesApi messagesApi) {
        this.messagesApi = messagesApi;
    }

    @Override
    public Formatters get() {
        Formatters formatters = new Formatters(messagesApi);
        formatters.register(Instant.class, new InstantSimpleFormatter());
        return formatters;
    }
}
