package data.entities.voting;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "auth_token")
public class JpaAuthToken {
    @Id
    @Column(name = "token")
    private UUID token;

    @ManyToOne
    @JoinColumn(name = "voter_id")
    private JpaVoter voter;

    @ManyToOne()
    @JoinColumn(name = "voting_id")
    private JpaVoting voting;

    public UUID getToken() {
        return token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    public JpaVoter getVoter() {
        return voter;
    }

    public void setVoter(JpaVoter voter) {
        this.voter = voter;
    }

    public JpaVoting getVoting() {
        return voting;
    }

    public void setVoting(JpaVoting voting) {
        this.voting = voting;
    }
}
