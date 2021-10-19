package dto;

import play.data.validation.Constraints;

public class CreateVotingRequest {
    @Constraints.Required
    private String network;

    @Constraints.Required
    @Constraints.Min(2)
    private Long votesCap;

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public Long getVotesCap() {
        return votesCap;
    }

    public void setVotesCap(Long votesCap) {
        this.votesCap = votesCap;
    }

    @Override
    public String toString() {
        return "CreateVotingRequest{" +
                "network='" + network + '\'' +
                ", votesCap=" + votesCap +
                '}';
    }
}
