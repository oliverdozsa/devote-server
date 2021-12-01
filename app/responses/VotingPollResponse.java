package responses;

import java.util.List;

public class VotingPollResponse {
    private String question;
    private List<VotingPollOptionResponse> pollOptions;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<VotingPollOptionResponse> getPollOptions() {
        return pollOptions;
    }

    public void setPollOptions(List<VotingPollOptionResponse> pollOptions) {
        this.pollOptions = pollOptions;
    }
}
