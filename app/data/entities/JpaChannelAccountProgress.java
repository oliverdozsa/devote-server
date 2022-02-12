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
    private JpaChannelGeneratorAccount issuer;

    @Column(name = "num_of_accounts_to_create")
    private Long numOfAccountsToCreate;

    @Column(name = "num_of_accounts_left_to_create")
    private Long numOfAccountsLeftToCreate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JpaChannelGeneratorAccount getIssuer() {
        return issuer;
    }

    public void setIssuer(JpaChannelGeneratorAccount issuer) {
        this.issuer = issuer;
    }

    public Long getNumOfAccountsToCreate() {
        return numOfAccountsToCreate;
    }

    public void setNumOfAccountsToCreate(Long numOfAccountsToCreate) {
        this.numOfAccountsToCreate = numOfAccountsToCreate;
    }

    public Long getNumOfAccountsLeftToCreate() {
        return numOfAccountsLeftToCreate;
    }

    public void setNumOfAccountsLeftToCreate(Long numOfAccountsLeftToCreate) {
        this.numOfAccountsLeftToCreate = numOfAccountsLeftToCreate;
    }
}
