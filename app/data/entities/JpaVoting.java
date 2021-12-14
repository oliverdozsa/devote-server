package data.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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

    @Column(name = "title")
    @Lob
    private String title;

    @Column(name = "network", nullable = false)
    private String network;

    @Column(name = "votes_cap", nullable = false)
    private Long votesCap;

    @OneToMany(mappedBy = "voting", cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    private List<JpaVotingIssuerAccount> issuerAccounts;

    @OneToMany(mappedBy = "voting", cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    private List<JpaVotingChannelAccount> channelAccounts;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "encryption_key")
    @Lob
    private String encryptionKey;

    @Column(name = "encrypted_until")
    private Instant encryptedUntil;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Column(name = "distribution_account_secret")
    @Lob
    private String distributionAccountSecret;

    @Column(name = "ballot_account_secret")
    @Lob
    private String ballotAccountSecret;

    @Column(name = "authorization", nullable = false)
    @Enumerated(EnumType.STRING)
    private Authorization authorization;

    @OneToMany(mappedBy = "voting", cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    private List<JpaVotingAuthorizationEmail> authOptionsEmails;

    @OneToOne(mappedBy = "voting")
    private JpaVotingAuthorizationKeybase authOptionKeybase;

    @OneToMany(mappedBy = "voting", cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    private List<JpaVotingPoll> polls;

    @Column(name = "visibility", nullable = false)
    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @Column(name = "ipfs_cid")
    private String ipfsCid;

    @OneToMany(mappedBy = "voting", cascade = {CascadeType.REMOVE})
    private List<JpaCommissionInitSession> initSessions;

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

    public Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(Authorization authorization) {
        this.authorization = authorization;
    }

    public List<JpaVotingAuthorizationEmail> getAuthOptionsEmails() {
        return authOptionsEmails;
    }

    public void setAuthOptionsEmails(List<JpaVotingAuthorizationEmail> authOptionsEmails) {
        this.authOptionsEmails = authOptionsEmails;
    }

    public JpaVotingAuthorizationKeybase getAuthOptionKeybase() {
        return authOptionKeybase;
    }

    public void setAuthOptionKeybase(JpaVotingAuthorizationKeybase authOptionKeybase) {
        this.authOptionKeybase = authOptionKeybase;
    }

    public List<JpaVotingPoll> getPolls() {
        return polls;
    }

    public void setPolls(List<JpaVotingPoll> polls) {
        this.polls = polls;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getIpfsCid() {
        return ipfsCid;
    }

    public void setIpfsCid(String ipfsCid) {
        this.ipfsCid = ipfsCid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<JpaCommissionInitSession> getInitSessions() {
        return initSessions;
    }

    public void setInitSessions(List<JpaCommissionInitSession> initSessions) {
        this.initSessions = initSessions;
    }
}
