import com.google.inject.name.Names;
import data.operations.CommissionDbOperations;
import data.repositories.ChannelProgressRepository;
import data.repositories.CommissionRepository;
import data.repositories.imp.CommissionRepositoryImp;
import data.repositories.imp.EbeanChannelProgressRepository;
import devote.blockchain.operations.VotingBlockchainOperations;
import data.operations.VotingDbOperations;
import data.repositories.VotingRepository;
import data.repositories.imp.EbeanVotingRepository;
import devote.blockchain.Blockchains;
import com.google.inject.AbstractModule;
import formatters.FormattersProvider;
import ipfs.VotingIpfsOperations;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import play.data.format.Formatters;
import security.JwtCenter;
import services.CommissionService;
import services.EnvelopKeyPairProvider;
import services.VotingService;
import tasks.channelaccounts.ChannelAccountBuilderTaskContext;
import tasks.channelaccounts.ChannelAccountTasksOrganizer;

import java.security.Security;

public class Module extends AbstractModule {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    protected void configure() {
        // Formatters
        bind(Formatters.class).toProvider(FormattersProvider.class);

        bind(Blockchains.class).asEagerSingleton();

        // Data
        bind(VotingRepository.class).to(EbeanVotingRepository.class).asEagerSingleton();
        bind(ChannelProgressRepository.class).to(EbeanChannelProgressRepository.class).asEagerSingleton();
        bind(CommissionRepository.class).to(CommissionRepositoryImp.class).asEagerSingleton();

        // Operations
        bind(VotingDbOperations.class).asEagerSingleton();
        bind(CommissionDbOperations.class).asEagerSingleton();
        bind(VotingBlockchainOperations.class).asEagerSingleton();
        bind(VotingIpfsOperations.class).asEagerSingleton();

        // Services
        bind(VotingService.class).asEagerSingleton();
        bind(CommissionService.class).asEagerSingleton();

        // Tasks
        bind(ChannelAccountTasksOrganizer.class).asEagerSingleton();
        bind(ChannelAccountBuilderTaskContext.class).asEagerSingleton();

        // Other
        bind(AsymmetricCipherKeyPair.class).annotatedWith(Names.named("envelope"))
                .toProvider(EnvelopKeyPairProvider.class)
                .asEagerSingleton();
        bind(JwtCenter.class).asEagerSingleton();
    }
}
