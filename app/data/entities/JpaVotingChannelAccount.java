package data.entities;

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
@Table(name = "voting_channel_account")
public class JpaVotingChannelAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "account_secret")
    @Lob
    private String accountSecret;

    @ManyToOne
    @JoinColumn(name = "voting_id")
    private JpaVoting voting;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountSecret() {
        return accountSecret;
    }

    public void setAccountSecret(String accountSecret) {
        this.accountSecret = accountSecret;
    }

    public JpaVoting getVoting() {
        return voting;
    }

    public void setVoting(JpaVoting voting) {
        this.voting = voting;
    }
}
