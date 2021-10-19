import data.operations.VotingBlockchainOperations;
import data.operations.VotingDbOperations;
import data.repositories.VotingRepository;
import data.repositories.imp.EbeanVotingRepository;
import devote.blockchain.Blockchains;
import com.google.inject.AbstractModule;
import services.VotingService;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(Blockchains.class).asEagerSingleton();

        // Data
        bind(VotingRepository.class).to(EbeanVotingRepository.class).asEagerSingleton();

        bind(VotingDbOperations.class).asEagerSingleton();
        bind(VotingBlockchainOperations.class).asEagerSingleton();

        // Services
        bind(VotingService.class).asEagerSingleton();
    }
}
