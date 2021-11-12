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

    @Constraints.Pattern("[0-9a-z]+")
    @Constraints.MaxLength(8)
    @Constraints.MinLength(2)
    private String tokenIdentifier;

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

    public String getTokenIdentifier() {
        return tokenIdentifier;
    }

    public void setTokenIdentifier(String tokenIdentifier) {
        this.tokenIdentifier = tokenIdentifier;
    }

    @Override
    public String toString() {
        return "CreateVotingRequest{" +
                "network='" + network + '\'' +
                ", votesCap=" + votesCap +
                ", title='" + title + '\'' +
                ", tokenIdentifier='" + tokenIdentifier + '\'' +
                '}';
    }
}
