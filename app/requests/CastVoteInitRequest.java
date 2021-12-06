package requests;

public class CastVoteInitRequest {
    private String votingId;

    public String getVotingId() {
        return votingId;
    }

    public void setVotingId(String votingId) {
        this.votingId = votingId;
    }

    @Override
    public String toString() {
        return "CastVoteInitRequest{" +
                "votingId='" + votingId + '\'' +
                '}';
    }
}
