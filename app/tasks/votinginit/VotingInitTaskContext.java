package tasks.votinginit;

import com.typesafe.config.Config;
import data.repositories.VotingRepository;
import devote.blockchain.Blockchains;

import javax.inject.Inject;

public class VotingInitTaskContext {
    public final Blockchains blockchains;
    public final VotingRepository votingRepository;
    public final int voteBuckets;

    @Inject
    public VotingInitTaskContext(Blockchains blockchains, VotingRepository votingRepository, Config config) {
        this.blockchains = blockchains;
        this.votingRepository = votingRepository;
        this.voteBuckets = config.getInt("devote.vote.buckets");
    }
}
