package ipfs.data;

import data.entities.Authorization;
import data.entities.JpaVoting;
import data.entities.JpaVotingIssuerAccount;
import data.entities.JpaVotingPoll;
import data.entities.JpaVotingPollOption;
import devote.blockchain.BlockchainFactory;
import devote.blockchain.Blockchains;
import devote.blockchain.api.DistributionAndBallotAccount;
import devote.blockchain.api.IssuerAccount;

import java.util.List;
import java.util.stream.Collectors;

public class IpfsVotingFromJpaVoting {
    private final Blockchains blockchains;

    public IpfsVotingFromJpaVoting(Blockchains blockchains) {
        this.blockchains = blockchains;
    }

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
        setIssuers(ipfsVoting, jpaVoting);
        ipfsVoting.setCreatedAt(jpaVoting.getCreatedAt());
        ipfsVoting.setEncryptedUntil(jpaVoting.getEncryptedUntil());
        ipfsVoting.setStartDate(jpaVoting.getStartDate());
        ipfsVoting.setEndDate(jpaVoting.getEndDate());
        setDistributionAndBallotAccountId(ipfsVoting, jpaVoting);
        ipfsVoting.setAuthorization(jpaVoting.getAuthorization().name());
        setAuthOptionKeybase(ipfsVoting, jpaVoting);
        ipfsVoting.setVisibility(jpaVoting.getVisibility().name());
    }

    private static void setPollData(IpfsVoting ipfsVoting, JpaVoting jpaVoting) {
        List<IpfsPoll> ipfsPolls = jpaVoting.getPolls().stream()
                .map(IpfsVotingFromJpaVoting::toIpfsPoll)
                .collect(Collectors.toList());
        ipfsVoting.setPolls(ipfsPolls);
    }

    private void setIssuers(IpfsVoting ipfsVoting, JpaVoting jpaVoting) {
        List<IpfsVotingIssuer> ipfsVotingIssuers = jpaVoting.getIssuerAccounts().stream()
                .map(this::toIpfsVotingIssuer)
                .collect(Collectors.toList());
        ipfsVoting.setIssuers(ipfsVotingIssuers);
    }

    private IpfsVotingIssuer toIpfsVotingIssuer(JpaVotingIssuerAccount jpaVotingIssuer) {
        String network = jpaVotingIssuer.getVoting().getNetwork();
        BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(network);

        IssuerAccount issuerAccount = blockchainFactory.createIssuerAccount();
        String issuerPublic = issuerAccount.toPublicAccountId(jpaVotingIssuer.getAccountSecret());

        IpfsVotingIssuer ipfsVotingIssuer = new IpfsVotingIssuer();
        ipfsVotingIssuer.setIssuerAccountId(issuerPublic);
        ipfsVotingIssuer.setAssetCode(jpaVotingIssuer.getAssetCode());

        return ipfsVotingIssuer;
    }

    private void setDistributionAndBallotAccountId(IpfsVoting ipfsVoting, JpaVoting jpaVoting) {
        BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(jpaVoting.getNetwork());
        DistributionAndBallotAccount distributionAndBallotAccount = blockchainFactory.createDistributionAndBallotAccount();

        String distributionAccountSecret = jpaVoting.getDistributionAccountSecret();
        String distributionAccountPublic = distributionAndBallotAccount.toPublicDistributionAccountId(distributionAccountSecret);
        ipfsVoting.setDistributionAccountId(distributionAccountPublic);

        String ballotAccountSecret = jpaVoting.getBallotAccountSecret();
        String ballotAccountPublic = distributionAndBallotAccount.toPublicBallotAccountId(ballotAccountSecret);
        ipfsVoting.setBallotAccountId(ballotAccountPublic);
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

    private static void setAuthOptionKeybase(IpfsVoting ipfsVoting, JpaVoting jpaVoting) {
        if(jpaVoting.getAuthorization() == Authorization.KEYBASE) {
            ipfsVoting.setAuthOptionKeybase(jpaVoting.getAuthOptionKeybase().getTeamName());
        }
    }
}
