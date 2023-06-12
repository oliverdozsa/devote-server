package tasks.voting.refundbalances;

import data.repositories.ChannelAccountRepository;
import data.repositories.ChannelGeneratorAccountRepository;
import data.repositories.VotingRepository;
import galactic.blockchain.Blockchains;

import javax.inject.Inject;

public class RefundBalancesTaskContext {
    public final Blockchains blockchains;
    public final VotingRepository votingRepository;
    public final ChannelAccountRepository channelAccountRepository;
    public final ChannelGeneratorAccountRepository channelGeneratorAccountRepository;

    @Inject
    public RefundBalancesTaskContext(Blockchains blockchains, VotingRepository votingRepository,
                                     ChannelAccountRepository channelAccountRepository,
                                     ChannelGeneratorAccountRepository channelGeneratorAccountRepository) {
        this.blockchains = blockchains;
        this.votingRepository = votingRepository;
        this.channelAccountRepository = channelAccountRepository;
        this.channelGeneratorAccountRepository = channelGeneratorAccountRepository;
    }


}
