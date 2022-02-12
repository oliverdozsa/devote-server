package responses;

import data.entities.Authorization;
import data.entities.JpaVoting;
import data.entities.JpaVotingPoll;
import data.entities.JpaVotingPollOption;

import java.util.List;
import java.util.stream.Collectors;

public class VotingResponseFromJpaVoting {
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
        votingResponse.setCreatedAt(jpaVoting.getCreatedAt());
        votingResponse.setEncryptedUntil(jpaVoting.getEncryptedUntil());
        votingResponse.setStartDate(jpaVoting.getStartDate());
        votingResponse.setEndDate(jpaVoting.getEndDate());
        setDistributionAndBallotAccountId(votingResponse, jpaVoting);
        votingResponse.setAuthorization(jpaVoting.getAuthorization().name());
        setAuthOptionKeybase(votingResponse, jpaVoting);
        votingResponse.setVisibility(jpaVoting.getVisibility().name());
        votingResponse.setIssuerAccountId(jpaVoting.getIssuerAccountPublic());
        votingResponse.setAssetCode(jpaVoting.getAssetCode());
    }

    private void setDistributionAndBallotAccountId(VotingResponse ipfsVoting, JpaVoting jpaVoting) {
        String distributionAccountPublic = jpaVoting.getDistributionAccountPublic();
        ipfsVoting.setDistributionAccountId(distributionAccountPublic);

        String ballotAccountPublic = jpaVoting.getBallotAccountPublic();
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
