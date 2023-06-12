package requests.voting;

import play.data.validation.Constraints;

public class CommissionInitRequest {
    @Constraints.Required
    private String votingId;

    public String getVotingId() {
        return votingId;
    }

    public void setVotingId(String votingId) {
        this.votingId = votingId;
    }

    @Override
    public String toString() {
        return "CommissionInitRequest{" +
                "votingId='" + votingId + '\'' +
                '}';
    }
}
