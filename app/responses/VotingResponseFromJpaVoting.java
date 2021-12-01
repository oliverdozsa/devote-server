package responses;

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

public class VotingResponseFromJpaVoting {
    private final Blockchains blockchains;

    public VotingResponseFromJpaVoting(Blockchains blockchains) {
        this.blockchains = blockchains;
    }

    public VotingResponse convert(JpaVoting jpaVoting) {
        VotingResponse votingResponse = new VotingResponse();

        setBasicData(votingResponse, jpaVoting);
        setPollData(votingResponse, jpaVoting);

        return votingResponse;
    }

    private void setBasicData(VotingResponse votingResponse, JpaVoting jpaVoting) {
        votingResponse.setId(jpaVoting.getId());
        votingResponse.setTitle(jpaVoting.getTitle());
        votingResponse.setNetwork(jpaVoting.getNetwork());
        votingResponse.setVotesCap(jpaVoting.getVotesCap());
        setIssuers(votingResponse, jpaVoting);
        votingResponse.setCreatedAt(jpaVoting.getCreatedAt());
        votingResponse.setEncryptedUntil(jpaVoting.getEncryptedUntil());
        votingResponse.setStartDate(jpaVoting.getStartDate());
        votingResponse.setEndDate(jpaVoting.getEndDate());
        setDistributionAndBallotAccountId(votingResponse, jpaVoting);
        votingResponse.setAuthorization(jpaVoting.getAuthorization().name());
        setAuthOptionKeybase(votingResponse, jpaVoting);
        votingResponse.setVisibility(jpaVoting.getVisibility().name());
    }

    private void setIssuers(VotingResponse votingResponse, JpaVoting jpaVoting) {
        List<VotingIssuerResponse> votingResponseIssuers = jpaVoting.getIssuerAccounts().stream()
                .map(this::toVotingResponseIssuer)
                .collect(Collectors.toList());
        votingResponse.setIssuers(votingResponseIssuers);
    }

    private VotingIssuerResponse toVotingResponseIssuer(JpaVotingIssuerAccount jpaVotingIssuer) {
        String network = jpaVotingIssuer.getVoting().getNetwork();
        BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(network);

        IssuerAccount issuerAccount = blockchainFactory.createIssuerAccount();
        String issuerPublic = issuerAccount.toPublicAccountId(jpaVotingIssuer.getAccountSecret());

        VotingIssuerResponse votingIssuerResponse = new VotingIssuerResponse();
        votingIssuerResponse.setIssuerAccountId(issuerPublic);
        votingIssuerResponse.setAssetCode(jpaVotingIssuer.getAssetCode());

        return votingIssuerResponse;
    }

    private void setDistributionAndBallotAccountId(VotingResponse ipfsVoting, JpaVoting jpaVoting) {
        BlockchainFactory blockchainFactory = blockchains.getFactoryByNetwork(jpaVoting.getNetwork());
        DistributionAndBallotAccount distributionAndBallotAccount = blockchainFactory.createDistributionAndBallotAccount();

        String distributionAccountSecret = jpaVoting.getDistributionAccountSecret();
        String distributionAccountPublic = distributionAndBallotAccount.toPublicDistributionAccountId(distributionAccountSecret);
        ipfsVoting.setDistributionAccountId(distributionAccountPublic);

        String ballotAccountSecret = jpaVoting.getBallotAccountSecret();
        String ballotAccountPublic = distributionAndBallotAccount.toPublicBallotAccountId(ballotAccountSecret);
        ipfsVoting.setBallotAccountId(ballotAccountPublic);
    }

    private static void setAuthOptionKeybase(VotingResponse ipfsVoting, JpaVoting jpaVoting) {
        if(jpaVoting.getAuthorization() == Authorization.KEYBASE) {
            ipfsVoting.setAuthOptionKeybase(jpaVoting.getAuthOptionKeybase().getTeamName());
        }
    }

    private void setPollData(VotingResponse votingResponse, JpaVoting jpaVoting) {
        List<VotingPollResponse> votingPollResponses = jpaVoting.getPolls().stream()
                .map(VotingResponseFromJpaVoting::toVotingPollResponse)
                .collect(Collectors.toList());
        votingResponse.setPolls(votingPollResponses);
    }

    private static VotingPollResponse toVotingPollResponse(JpaVotingPoll jpaVotingPoll) {
        VotingPollResponse votingPollResponse = new VotingPollResponse();

        votingPollResponse.setQuestion(jpaVotingPoll.getQuestion());

        List<VotingPollOptionResponse> votingPollOptionResponses = jpaVotingPoll.getOptions().stream()
                .map(VotingResponseFromJpaVoting::toVotingPollOptionResponse)
                .collect(Collectors.toList());
        votingPollResponse.setPollOptions(votingPollOptionResponses);

        return votingPollResponse;
    }

    private static VotingPollOptionResponse toVotingPollOptionResponse(JpaVotingPollOption jpaPollOption) {
        VotingPollOptionResponse votingPollOptionResponse = new VotingPollOptionResponse();

        votingPollOptionResponse.setCode(jpaPollOption.getCode());
        votingPollOptionResponse.setName(jpaPollOption.getName());

        return votingPollOptionResponse;
    }
}
