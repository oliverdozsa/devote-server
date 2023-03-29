package tasks.channelaccounts;

import com.typesafe.config.Config;
import data.repositories.ChannelProgressRepository;
import data.repositories.VotingRepository;
import galactic.blockchain.Blockchains;

import javax.inject.Inject;

public class ChannelAccountBuilderTaskContext {
    public final Blockchains blockchains;
    public final VotingRepository votingRepository;
    public final ChannelProgressRepository channelProgressRepository;
    public final int voteBuckets;

    @Inject
    public ChannelAccountBuilderTaskContext(
            Blockchains blockchains,
            VotingRepository votingRepository,
            ChannelProgressRepository channelProgressRepository,
            Config config) {
        this.blockchains = blockchains;
        this.votingRepository = votingRepository;
        this.channelProgressRepository = channelProgressRepository;
        this.voteBuckets = config.getInt("galactic.vote.vote.buckets");
    }
}
