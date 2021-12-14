package data.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
        name = "commission_init_session",
        uniqueConstraints = @UniqueConstraint(columnNames = {"voting_id", "user_id"})
)
public class JpaCommissionInitSession {
    @ManyToOne
    @JoinColumn(name = "voting_id")
    private JpaVoting voting;

    @Column(name = "user_id")
    private String userId;

    public JpaVoting getVoting() {
        return voting;
    }

    public void setVoting(JpaVoting voting) {
        this.voting = voting;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
