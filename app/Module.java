import blockchain.BlockhainDiscovery;
import com.google.inject.AbstractModule;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(BlockhainDiscovery.class).asEagerSingleton();
    }
}
