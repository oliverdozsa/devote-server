import devote.blockchain.Blockchains;
import com.google.inject.AbstractModule;
import services.VotingService;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(Blockchains.class).asEagerSingleton();

        // Services
        bind(VotingService.class).asEagerSingleton();
    }
}
