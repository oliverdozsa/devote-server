package ipfs.data;

import data.entities.Authorization;
import data.entities.JpaVoting;
import data.entities.JpaChannelGeneratorAccount;
import data.entities.JpaVotingPoll;
import data.entities.JpaVotingPollOption;

import java.util.List;
import java.util.stream.Collectors;

public class IpfsVotingFromJpaVoting {
    public IpfsVoting convert(JpaVoting jpaVoting) {
        IpfsVoting ipfsVoting = new IpfsVoting();

        setBasicData(ipfsVoting, jpaVoting);
        setPollData(ipfsVoting, jpaVoting);

        return ipfsVoting;
    }

    private void setBasicData(IpfsVoting ipfsVoting, JpaVoting jpaVoting) {
        ipfsVoting.setTitle(jpaVoting.getTitle());
        ipfsVoting.setNetwork(jpaVoting.getNetwork());
        ipfsVoting.setVotesCap(jpaVoting.getVotesCap());
        ipfsVoting.setCreatedAt(jpaVoting.getCreatedAt());
        ipfsVoting.setEncryptedUntil(jpaVoting.getEncryptedUntil());
        ipfsVoting.setStartDate(jpaVoting.getStartDate());
        ipfsVoting.setEndDate(jpaVoting.getEndDate());
        setDistributionAndBallotAccountId(ipfsVoting, jpaVoting);
        ipfsVoting.setAuthorization(jpaVoting.getAuthorization().name());
        ipfsVoting.setVisibility(jpaVoting.getVisibility().name());
        ipfsVoting.setIssuerAccountId(jpaVoting.getIssuerAccountPublic());
        ipfsVoting.setAssetCode(jpaVoting.getAssetCode());
    }

    private void setDistributionAndBallotAccountId(IpfsVoting ipfsVoting, JpaVoting jpaVoting) {
        String distributionAccountPublic = jpaVoting.getDistributionAccountPublic();
        ipfsVoting.setDistributionAccountId(distributionAccountPublic);

        String ballotAccountPublic = jpaVoting.getBallotAccountPublic();
        ipfsVoting.setBallotAccountId(ballotAccountPublic);
    }

    private static void setPollData(IpfsVoting ipfsVoting, JpaVoting jpaVoting) {
        List<IpfsPoll> ipfsPolls = jpaVoting.getPolls().stream()
                .map(IpfsVotingFromJpaVoting::toIpfsPoll)
                .collect(Collectors.toList());
        ipfsVoting.setPolls(ipfsPolls);
    }

    private static IpfsPoll toIpfsPoll(JpaVotingPoll jpaVotingPoll) {
        IpfsPoll ipfsPoll = new IpfsPoll();

        ipfsPoll.setQuestion(jpaVotingPoll.getQuestion());

        List<IpfsPollOption> ipfsPollOptions = jpaVotingPoll.getOptions().stream()
                .map(IpfsVotingFromJpaVoting::toIpfsPollOption)
                .collect(Collectors.toList());
        ipfsPoll.setPollOptions(ipfsPollOptions);

        return ipfsPoll;
    }

    private static IpfsPollOption toIpfsPollOption(JpaVotingPollOption jpaPollOption) {
        IpfsPollOption ipfsPollOption = new IpfsPollOption();

        ipfsPollOption.setCode(jpaPollOption.getCode());
        ipfsPollOption.setName(jpaPollOption.getName());

        return ipfsPollOption;
    }
}
