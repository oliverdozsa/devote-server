package ipfs.data;

import java.util.List;

public class IpfsPoll {
    private String question;
    private List<IpfsPollOption> pollOptions;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<IpfsPollOption> getPollOptions() {
        return pollOptions;
    }

    public void setPollOptions(List<IpfsPollOption> pollOptions) {
        this.pollOptions = pollOptions;
    }
}
