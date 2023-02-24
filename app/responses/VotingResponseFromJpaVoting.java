package responses;

import data.entities.JpaVoting;
import data.entities.JpaVotingPoll;
import data.entities.JpaVotingPollOption;
import play.Logger;
import services.Base62Conversions;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class VotingResponseFromJpaVoting {
    private static final Logger.ALogger logger = Logger.of(VotingResponseFromJpaVoting.class);

    public VotingResponse convert(JpaVoting jpaVoting) {
        VotingResponse votingResponse = new VotingResponse();

        setBasicData(votingResponse, jpaVoting);
        setPollData(votingResponse, jpaVoting);

        return votingResponse;
    }

    private void setBasicData(VotingResponse votingResponse, JpaVoting jpaVoting) {
        votingResponse.setId(Base62Conversions.encode(jpaVoting.getId()));
        votingResponse.setTitle(jpaVoting.getTitle());
        votingResponse.setNetwork(jpaVoting.getNetwork());
        votingResponse.setVotesCap(jpaVoting.getVotesCap());
        votingResponse.setCreatedAt(jpaVoting.getCreatedAt());
        votingResponse.setEncryptedUntil(jpaVoting.getEncryptedUntil());
        votingResponse.setStartDate(jpaVoting.getStartDate());
        votingResponse.setEndDate(jpaVoting.getEndDate());
        setDistributionAndBallotAccountId(votingResponse, jpaVoting);
        votingResponse.setAuthorization(jpaVoting.getAuthorization().name());
        votingResponse.setVisibility(jpaVoting.getVisibility().name());
        votingResponse.setIssuerAccountId(jpaVoting.getIssuerAccountPublic());
        votingResponse.setAssetCode(jpaVoting.getAssetCode());
        votingResponse.setOnTestNetwork(jpaVoting.getOnTestNetwork());
        votingResponse.setFundingAccountId(jpaVoting.getUserGivenFundingAccountPublic());
        votingResponse.setRefunded(jpaVoting.isInternalFundingRefunded());
        votingResponse.setInvitesBased(jpaVoting.isAuthTokenBased());
        setDecryptionKeyIfNeeded(votingResponse, jpaVoting);
        votingResponse.setBallotType(jpaVoting.getBallotType().name());
        votingResponse.setMaxChoices(jpaVoting.getMaxChoices());
        votingResponse.setDescription(jpaVoting.getDescription());
    }

    private void setDistributionAndBallotAccountId(VotingResponse ipfsVoting, JpaVoting jpaVoting) {
        String distributionAccountPublic = jpaVoting.getDistributionAccountPublic();
        ipfsVoting.setDistributionAccountId(distributionAccountPublic);

        String ballotAccountPublic = jpaVoting.getBallotAccountPublic();
        ipfsVoting.setBallotAccountId(ballotAccountPublic);
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
        votingPollResponse.setDescription(jpaVotingPoll.getDescription());
        votingPollResponse.setIndex(jpaVotingPoll.getIndex());

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

    private static void setDecryptionKeyIfNeeded(VotingResponse votingResponse, JpaVoting jpaVoting) {
        if(jpaVoting.getEncryptedUntil() != null && jpaVoting.getEncryptedUntil().compareTo(Instant.now()) <= 0) {
            logger.info("Encrypted until expired for voting {}, giving decryption key in response.", jpaVoting.getId());
            votingResponse.setDecryptionKey(jpaVoting.getEncryptionKey());
        }
    }
}
