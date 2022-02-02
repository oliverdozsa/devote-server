package data.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "voting_issuer_account")
public class JpaVotingIssuerAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "voting_id")
    private JpaVoting voting;

    @Column(name = "account_secret")
    @Lob
    private String accountSecret;

    @Column(name = "account_public")
    @Lob
    private String accountPublic;

    @Column(name = "asset_code", length = 20)
    private String assetCode;

    @Column(name = "votes_cap")
    private Long votesCap;

    @OneToOne(mappedBy = "issuer")
    private JpaChannelAccountProgress channelAccountProgress;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JpaVoting getVoting() {
        return voting;
    }

    public void setVoting(JpaVoting voting) {
        this.voting = voting;
    }

    public String getAccountSecret() {
        return accountSecret;
    }

    public void setAccountSecret(String accountSecret) {
        this.accountSecret = accountSecret;
    }

    public JpaChannelAccountProgress getChannelAccountProgress() {
        return channelAccountProgress;
    }

    public void setChannelAccountProgress(JpaChannelAccountProgress channelAccountProgress) {
        this.channelAccountProgress = channelAccountProgress;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public String getAccountPublic() {
        return accountPublic;
    }

    public void setAccountPublic(String accountPublic) {
        this.accountPublic = accountPublic;
    }

    public Long getVotesCap() {
        return votesCap;
    }

    public void setVotesCap(Long votesCap) {
        this.votesCap = votesCap;
    }
}
