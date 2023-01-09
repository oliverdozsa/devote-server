package data.entities;

import javax.persistence.*;

@Entity()
@Table(name = "voter_user_id")
public class JpaVoterUserId {
    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne
    @JoinColumn(name = "voter_id")
    private JpaVoter voter;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JpaVoter getVoter() {
        return voter;
    }

    public void setVoter(JpaVoter voter) {
        this.voter = voter;
    }
}
