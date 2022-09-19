package responses;

import java.util.List;

public class VotingPollResponse {
    private Integer index;
    private String question;
    private List<VotingPollOptionResponse> pollOptions;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

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
