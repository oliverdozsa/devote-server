import devote.blockchain.operations.VotingBlockchainOperations;
import data.operations.VotingDbOperations;
import data.repositories.VotingRepository;
import data.repositories.imp.EbeanVotingRepository;
import devote.blockchain.Blockchains;
import com.google.inject.AbstractModule;
import services.VotingService;
import tasks.channelaccounts.ChannelAccountTasksOrganizer;

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

        // Tasks
        bind(ChannelAccountTasksOrganizer.class).asEagerSingleton();
    }
}
