package data.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.List;

@Entity
@Table(
        name = "voter",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "email"})
        }
)
public class JpaVoter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "email")
    private String email;

    @ManyToMany
    @JoinTable(
            name = "votings_participants",
            joinColumns = @JoinColumn(name = "voter_id"),
            inverseJoinColumns = @JoinColumn(name = "voting_id")
    )
    private List<JpaVoting> votings;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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
