import com.auth0.jwk.JwkProvider;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import data.operations.CommissionDbOperations;
import data.operations.PageOfVotingsDbOperations;
import data.repositories.ChannelProgressRepository;
import data.repositories.CommissionRepository;
import data.repositories.PageOfVotingsRepository;
import data.repositories.VoterRepository;
import data.repositories.imp.EbeanCommissionRepository;
import data.repositories.imp.EbeanChannelProgressRepository;
import data.repositories.imp.EbeanPageOfVotingRepository;
import data.repositories.imp.EbeanServerProvider;
import data.repositories.imp.EbeanVoterRepository;
import devote.blockchain.operations.CommissionBlockchainOperations;
import devote.blockchain.operations.VotingBlockchainOperations;
import data.operations.VotingDbOperations;
import data.repositories.VotingRepository;
import data.repositories.imp.EbeanVotingRepository;
import devote.blockchain.Blockchains;
import com.google.inject.AbstractModule;
import formatters.FormattersProvider;
import io.ebean.EbeanServer;
import io.ipfs.api.IPFS;
import ipfs.api.IpfsApi;
import ipfs.api.imp.IpfsApiImp;
import ipfs.api.imp.IpfsProvider;
import ipfs.api.imp.PinataIpfsApiImp;
import ipfs.api.imp.Web3StorageIpfsApiImp;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import play.Environment;
import play.data.format.Formatters;
import security.JwtCenter;
import security.jwtverification.Auth0JwtVerification;
import security.jwtverification.JwkProviderProvider;
import security.jwtverification.JwtVerification;
import security.jwtverification.JwtVerificationForScaleTesting;
import services.CommissionService;
import services.EnvelopKeyPairProvider;
import services.VotingService;
import services.commissionsubs.userinfo.Auth0UserInfoCollector;
import services.commissionsubs.userinfo.UserInfoCollector;
import services.commissionsubs.userinfo.UserInfoCollectorForScaleTesting;
import tasks.TasksOrganizer;
import tasks.channelaccounts.ChannelAccountBuilderTaskContext;
import tasks.votingblockchaininit.VotingBlockchainInitTaskContext;

import java.security.Security;

public class Module extends AbstractModule {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final Environment environment;
    private final Config config;

    public Module(Environment environment, Config config) {
        this.environment = environment;
        this.config = config;
    }

    @Override
    protected void configure() {
        // Formatters
        bind(Formatters.class).toProvider(FormattersProvider.class);

        bind(Blockchains.class).asEagerSingleton();

        // Data
        bind(EbeanServer.class).toProvider(EbeanServerProvider.class);
        bind(VotingRepository.class).to(EbeanVotingRepository.class).asEagerSingleton();
        bind(ChannelProgressRepository.class).to(EbeanChannelProgressRepository.class).asEagerSingleton();
        bind(CommissionRepository.class).to(EbeanCommissionRepository.class).asEagerSingleton();
        bind(VoterRepository.class).to(EbeanVoterRepository.class).asEagerSingleton();
        bind(PageOfVotingsRepository.class).to(EbeanPageOfVotingRepository.class).asEagerSingleton();

        // Operations
        bind(VotingDbOperations.class).asEagerSingleton();
        bind(CommissionDbOperations.class).asEagerSingleton();
        bind(PageOfVotingsDbOperations.class).asEagerSingleton();
        bind(VotingBlockchainOperations.class).asEagerSingleton();
        bind(CommissionBlockchainOperations.class).asEagerSingleton();

        // Services
        bind(VotingService.class).asEagerSingleton();
        bind(CommissionService.class).asEagerSingleton();

        // Tasks
        bind(ChannelAccountBuilderTaskContext.class).asEagerSingleton();
        bind(VotingBlockchainInitTaskContext.class).asEagerSingleton();
        bind(TasksOrganizer.class).asEagerSingleton();

        // Auth
        if(config.getBoolean("devote.scale.test.mode")) {
            bind(JwtVerification.class).to(JwtVerificationForScaleTesting.class).asEagerSingleton();
            bind(UserInfoCollector.class).to(UserInfoCollectorForScaleTesting.class).asEagerSingleton();
        } else {
            bind(JwtVerification.class).to(Auth0JwtVerification.class).asEagerSingleton();
            bind(UserInfoCollector.class).to(Auth0UserInfoCollector.class).asEagerSingleton();
        }

        bind(JwkProvider.class).toProvider(JwkProviderProvider.class).asEagerSingleton();

        // Other
        bind(AsymmetricCipherKeyPair.class).annotatedWith(Names.named("envelope"))
                .toProvider(EnvelopKeyPairProvider.class)
                .asEagerSingleton();
        bind(JwtCenter.class).asEagerSingleton();
        bind(IPFS.class).toProvider(IpfsProvider.class);
        bind(IpfsApi.class).to(Web3StorageIpfsApiImp.class).asEagerSingleton();
    }
}
