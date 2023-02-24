package data.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "voting_poll")
public class JpaVotingPoll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "index", nullable = false)
    private Integer index;

    @Column(name = "question", nullable = false)
    @Lob
    private String question;

    @Column(name = "description")
    @Lob
    private String description;

    @OneToMany(mappedBy = "poll", cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    private List<JpaVotingPollOption> options;

    @ManyToOne(optional = false)
    @JoinColumn(name = "voting_id")
    private JpaVoting voting;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<JpaVotingPollOption> getOptions() {
        return options;
    }

    public void setOptions(List<JpaVotingPollOption> options) {
        this.options = options;
    }

    public JpaVoting getVoting() {
        return voting;
    }

    public void setVoting(JpaVoting voting) {
        this.voting = voting;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
