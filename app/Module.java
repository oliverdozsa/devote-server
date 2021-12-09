import data.repositories.ChannelProgressRepository;
import data.repositories.imp.EbeanChannelProgressRepository;
import devote.blockchain.operations.VotingBlockchainOperations;
import data.operations.VotingDbOperations;
import data.repositories.VotingRepository;
import data.repositories.imp.EbeanVotingRepository;
import devote.blockchain.Blockchains;
import com.google.inject.AbstractModule;
import formatters.FormattersProvider;
import ipfs.VotingIpfsOperations;
import play.data.format.Formatters;
import services.CastVoteService;
import services.VotingService;
import tasks.channelaccounts.ChannelAccountBuilderTaskContext;
import tasks.channelaccounts.ChannelAccountTasksOrganizer;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        // Formatters
        bind(Formatters.class).toProvider(FormattersProvider.class);

        bind(Blockchains.class).asEagerSingleton();

        // Data
        bind(VotingRepository.class).to(EbeanVotingRepository.class).asEagerSingleton();
        bind(ChannelProgressRepository.class).to(EbeanChannelProgressRepository.class).asEagerSingleton();

        bind(VotingDbOperations.class).asEagerSingleton();
        bind(VotingBlockchainOperations.class).asEagerSingleton();
        bind(VotingIpfsOperations.class).asEagerSingleton();

        // Services
        bind(VotingService.class).asEagerSingleton();
        bind(CastVoteService.class).asEagerSingleton();

        // Tasks
        bind(ChannelAccountTasksOrganizer.class).asEagerSingleton();
        bind(ChannelAccountBuilderTaskContext.class).asEagerSingleton();
    }
}
