import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import data.operations.voting.CommissionDbOperations;
import data.operations.voting.PageOfVotingsDbOperations;
import data.repositories.imp.*;
import data.repositories.imp.voting.*;
import data.repositories.voting.*;
import galactic.blockchain.operations.CommissionBlockchainOperations;
import galactic.blockchain.operations.VotingBlockchainOperations;
import data.operations.voting.VotingDbOperations;
import galactic.blockchain.Blockchains;
import com.google.inject.AbstractModule;
import formatters.FormattersProvider;
import io.ebean.EbeanServer;
import io.ipfs.api.IPFS;
import ipfs.api.IpfsApi;
import ipfs.api.imp.IpfsProvider;
import ipfs.api.imp.Web3StorageIpfsApiImp;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import play.Environment;
import play.data.format.Formatters;
import security.JwtCenter;
import security.TokenAuthAlgorithmProvider;
import security.TokenAuthUserIdUtil;
import security.jwtverification.*;
import services.voting.CommissionService;
import services.voting.EnvelopKeyPairProvider;
import services.voting.TokenAuthService;
import services.voting.VotingService;
import tasks.TasksOrganizer;
import tasks.voting.channelaccounts.ChannelAccountBuilderTaskContext;
import tasks.voting.emailinvites.EmailInvitesTaskContext;
import tasks.voting.refundbalances.RefundBalancesTaskContext;
import tasks.voting.tokenauthcleanup.TokenAuthCleanupTaskContext;
import tasks.voting.votingblockchaininit.VotingBlockchainInitTaskContext;

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
        bind(ChannelAccountRepository.class).to(EbeanChannelAccountRepository.class).asEagerSingleton();
        bind(ChannelGeneratorAccountRepository.class).to(EbeanChannelGeneratorAccountRepository.class).asEagerSingleton();
        bind(TokenAuthRepository.class).to(EbeanTokenAuthRepository.class).asEagerSingleton();

        // Operations
        bind(VotingDbOperations.class).asEagerSingleton();
        bind(CommissionDbOperations.class).asEagerSingleton();
        bind(PageOfVotingsDbOperations.class).asEagerSingleton();
        bind(VotingBlockchainOperations.class).asEagerSingleton();
        bind(CommissionBlockchainOperations.class).asEagerSingleton();

        // Services
        bind(VotingService.class).asEagerSingleton();
        bind(CommissionService.class).asEagerSingleton();
        bind(TokenAuthService.class).asEagerSingleton();

        // Tasks
        bind(ChannelAccountBuilderTaskContext.class).asEagerSingleton();
        bind(VotingBlockchainInitTaskContext.class).asEagerSingleton();
        bind(RefundBalancesTaskContext.class).asEagerSingleton();
        bind(EmailInvitesTaskContext.class).asEagerSingleton();
        bind(TokenAuthCleanupTaskContext.class).asEagerSingleton();
        bind(TasksOrganizer.class).asEagerSingleton();

        // Auth

        bind(JwtVerification.class)
                .annotatedWith(Names.named("auth0"))
                .to(Auth0JwtVerification.class)
                .asEagerSingleton();

        bind(JwtVerification.class)
                .annotatedWith(Names.named("tokenAuth"))
                .to(TokenAuthJwtVerification.class)
                .asEagerSingleton();
        bind(JwtVerification.class)
                .annotatedWith(Names.named("central"))
                .to(JwtCentralVerification.class)
                .asEagerSingleton();

        bind(JwkProvider.class).toProvider(JwkProviderProvider.class).asEagerSingleton();
        bind(Algorithm.class).annotatedWith(Names.named("tokenAuth"))
                .toProvider(TokenAuthAlgorithmProvider.class)
                .asEagerSingleton();
        bind(TokenAuthUserIdUtil.class).asEagerSingleton();

        // Other
        bind(AsymmetricCipherKeyPair.class).annotatedWith(Names.named("envelope"))
                .toProvider(EnvelopKeyPairProvider.class)
                .asEagerSingleton();
        bind(JwtCenter.class).asEagerSingleton();
        bind(IPFS.class).toProvider(IpfsProvider.class);
        bind(IpfsApi.class).to(Web3StorageIpfsApiImp.class).asEagerSingleton();
    }
}
