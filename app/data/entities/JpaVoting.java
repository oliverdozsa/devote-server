package data.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "voting")
public class JpaVoting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "network", nullable = false)
    private String network;

    @Column(name = "votes_cap", nullable = false)
    private Long votesCap;

    @OneToMany(mappedBy = "voting", cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    private List<JpaVotingIssuerAccount> issuerAccounts;

    @OneToMany(mappedBy = "voting", cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    private List<JpaVotingChannelAccount> channelAccounts;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "encryption_key")
    private String encryptionKey;

    @Column(name = "encrypted_until")
    private Instant encryptedUntil;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "distribution_account_secret")
    @Lob
    private String distributionAccountSecret;

    @Column(name = "ballot_account_secret")
    @Lob
    private String ballotAccountSecret;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public List<JpaVotingIssuerAccount> getIssuerAccounts() {
        return issuerAccounts;
    }

    public void setIssuerAccounts(List<JpaVotingIssuerAccount> issuerAccounts) {
        this.issuerAccounts = issuerAccounts;
    }

    public Long getVotesCap() {
        return votesCap;
    }

    public void setVotesCap(Long votesCap) {
        this.votesCap = votesCap;
    }

    public List<JpaVotingChannelAccount> getChannelAccounts() {
        return channelAccounts;
    }

    public void setChannelAccounts(List<JpaVotingChannelAccount> channelAccounts) {
        this.channelAccounts = channelAccounts;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getDistributionAccountSecret() {
        return distributionAccountSecret;
    }

    public void setDistributionAccountSecret(String distributionAccountSecret) {
        this.distributionAccountSecret = distributionAccountSecret;
    }

    public String getBallotAccountSecret() {
        return ballotAccountSecret;
    }

    public void setBallotAccountSecret(String ballotAccountSecret) {
        this.ballotAccountSecret = ballotAccountSecret;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public Instant getEncryptedUntil() {
        return encryptedUntil;
    }

    public void setEncryptedUntil(Instant encryptedUntil) {
        this.encryptedUntil = encryptedUntil;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }
}
