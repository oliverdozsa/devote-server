package tasks.votingblockchaininit;

import com.typesafe.config.Config;
import data.repositories.ChannelProgressRepository;
import data.repositories.VotingRepository;
import galactic.blockchain.Blockchains;
import ipfs.api.IpfsApi;

import javax.inject.Inject;

public class VotingBlockchainInitTaskContext {
    public final Blockchains blockchains;
    public final VotingRepository votingRepository;
    public final int voteBuckets;
    public final IpfsApi ipfsApi;
    public final ChannelProgressRepository channelProgressRepository;

    @Inject
    public VotingBlockchainInitTaskContext(Blockchains blockchains, VotingRepository votingRepository, Config config, IpfsApi ipfsApi, ChannelProgressRepository channelProgressRepository) {
        this.blockchains = blockchains;
        this.votingRepository = votingRepository;
        this.voteBuckets = config.getInt("galactic.host.vote.vote.buckets");
        this.ipfsApi = ipfsApi;
        this.channelProgressRepository = channelProgressRepository;
    }
}
