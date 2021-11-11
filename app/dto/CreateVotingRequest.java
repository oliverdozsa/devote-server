package dto;

import play.data.validation.Constraints;

public class CreateVotingRequest {
    @Constraints.Required
    private String network;

    @Constraints.Required
    @Constraints.Min(2)
    private Long votesCap;

    @Constraints.Required
    @Constraints.MinLength(2)
    private String title;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "CreateVotingRequest{" +
                "network='" + network + '\'' +
                ", votesCap=" + votesCap +
                ", title='" + title + '\'' +
                '}';
    }
}
