package data.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "channel_account_progress")
public class JpaChannelAccountProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "issuer_id")
    private JpaVotingIssuer issuer;

    @Column(name = "num_of_accounts_to_create")
    private Long numOfAccountToCreate;

    @Column(name = "num_of_accounts_left_to_create")
    private Long numOfAccountsToLeftToCreate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JpaVotingIssuer getIssuer() {
        return issuer;
    }

    public void setIssuer(JpaVotingIssuer issuer) {
        this.issuer = issuer;
    }

    public Long getNumOfAccountToCreate() {
        return numOfAccountToCreate;
    }

    public void setNumOfAccountToCreate(Long numOfAccountToCreate) {
        this.numOfAccountToCreate = numOfAccountToCreate;
    }

    public Long getNumOfAccountsToLeftToCreate() {
        return numOfAccountsToLeftToCreate;
    }

    public void setNumOfAccountsToLeftToCreate(Long numOfAccountsToLeftToCreate) {
        this.numOfAccountsToLeftToCreate = numOfAccountsToLeftToCreate;
    }
}
