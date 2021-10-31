package data.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
    private List<JpaVotingIssuer> issuers;

    @OneToMany(mappedBy = "voting", cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    private List<JpaVotingChannelAccount> channelAccounts;

    @Column(name = "created_at")
    private Instant createdAt;

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

    public List<JpaVotingIssuer> getIssuers() {
        return issuers;
    }

    public void setIssuers(List<JpaVotingIssuer> issuers) {
        this.issuers = issuers;
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
}
