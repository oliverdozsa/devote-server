package data.entities.voting;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "poll_option")
public class JpaVotingPollOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    @Lob
    private String name;

    @Column(name = "code", nullable = false)
    private Integer code;

    @ManyToOne(optional = false)
    @JoinColumn(name = "poll_id")
    private JpaVotingPoll poll;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public JpaVotingPoll getPoll() {
        return poll;
    }

    public void setPoll(JpaVotingPoll poll) {
        this.poll = poll;
    }
}
