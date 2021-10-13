package dto;

import play.data.validation.Constraints;

public class CreateVotingDto {
    @Constraints.Required
    private String network;

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }
}
