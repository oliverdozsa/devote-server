package tasks.voting.refundbalances;

import data.repositories.voting.ChannelAccountRepository;
import data.repositories.voting.ChannelGeneratorAccountRepository;
import data.repositories.voting.VotingRepository;
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
