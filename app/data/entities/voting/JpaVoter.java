package data.entities.voting;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "voter")
public class JpaVoter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email")
    private String email;

    @ManyToMany
    @JoinTable(
            name = "votings_participants",
            joinColumns = {@JoinColumn(name = "voter_id")},
            inverseJoinColumns = {@JoinColumn(name = "voting_id")}
    )
    private List<JpaVoting> votings;

    @OneToMany(mappedBy = "voter", cascade = {CascadeType.REMOVE})
    private List<JpaVoterUserId> voterIds;

    @OneToMany(mappedBy = "voter", cascade = {CascadeType.REMOVE})
    private List<JpaAuthToken> authTokens;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<JpaVoting> getVotings() {
        return votings;
    }

    public void setVotings(List<JpaVoting> votings) {
        this.votings = votings;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
