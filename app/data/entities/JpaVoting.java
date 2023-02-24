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
import javax.persistence.ManyToMany;
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

    @Column(name = "title")
    @Lob
    private String title;

    @Column(name = "network", nullable = false)
    private String network;

    @Column(name = "votes_cap", nullable = false)
    private Long votesCap;

    @OneToMany(mappedBy = "voting", cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    private List<JpaChannelGeneratorAccount> channelGeneratorAccounts;

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

    @Column(name = "distribution_account_public")
    @Lob
    private String distributionAccountPublic;

    @Column(name = "is_distribution_refunded")
    private boolean isDistributionRefunded;

    @Column(name = "ballot_account_secret")
    @Lob
    private String ballotAccountSecret;

    @Column(name = "ballot_account_public")
    @Lob
    private String ballotAccountPublic;

    @Column(name = "funding_account_public")
    @Lob
    private String fundingAccountPublic;

    @Column(name = "funding_account_secret")
    @Lob
    private String fundingAccountSecret;

    @Column(name = "asset_code", length = 20)
    private String assetCode;

    @Column(name = "issuer_account_public")
    @Lob
    private String issuerAccountPublic;

    @Column(name = "user_given_funding_account_public")
    @Lob
    private String userGivenFundingAccountPublic;

    @Column(name = "user_given_funding_account_secret")
    @Lob
    private String userGivenFundingAccountSecret;

    @Column(name = "auth_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Authorization authorization;

    @Column(name = "ballot_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private BallotType ballotType;

    @Column(name = "max_choices")
    private Integer maxChoices;

    @OneToMany(mappedBy = "voting", cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    private List<JpaVotingPoll> polls;

    @Column(name = "visibility", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @Column(name = "ipfs_cid")
    private String ipfsCid;

    @OneToMany(mappedBy = "voting", cascade = {CascadeType.REMOVE})
    private List<JpaCommissionSession> initSessions;

    @ManyToMany(mappedBy = "votings", cascade = {CascadeType.PERSIST})
    private List<JpaVoter> voters;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "is_on_test_network")
    private Boolean isOnTestNetwork;

    @Column(name = "is_internal_funding_refunded")
    private boolean isInternalFundingRefunded;

    @Column(name = "is_auth_token_based")
    private boolean isAuthTokenBased;

    @Column(name = "is_auth_tokens_need_to_be_created")
    private boolean isAuthTokensNeedToBeCreated;

    @Column(name = "organizer")
    private String organizer;

    @Column(name = "description")
    @Lob
    private String description;

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

    public List<JpaChannelGeneratorAccount> getChannelGeneratorAccounts() {
        return channelGeneratorAccounts;
    }

    public void setChannelGeneratorAccounts(List<JpaChannelGeneratorAccount> channelGeneratorAccounts) {
        this.channelGeneratorAccounts = channelGeneratorAccounts;
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

    public List<JpaCommissionSession> getInitSessions() {
        return initSessions;
    }

    public void setInitSessions(List<JpaCommissionSession> initSessions) {
        this.initSessions = initSessions;
    }

    public String getDistributionAccountPublic() {
        return distributionAccountPublic;
    }

    public void setDistributionAccountPublic(String distributionAccountPublic) {
        this.distributionAccountPublic = distributionAccountPublic;
    }

    public String getBallotAccountPublic() {
        return ballotAccountPublic;
    }

    public void setBallotAccountPublic(String ballotAccountPublic) {
        this.ballotAccountPublic = ballotAccountPublic;
    }

    public String getFundingAccountPublic() {
        return fundingAccountPublic;
    }

    public void setFundingAccountPublic(String fundingAccountPublic) {
        this.fundingAccountPublic = fundingAccountPublic;
    }

    public String getFundingAccountSecret() {
        return fundingAccountSecret;
    }

    public void setFundingAccountSecret(String fundingAccountSecret) {
        this.fundingAccountSecret = fundingAccountSecret;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public String getIssuerAccountPublic() {
        return issuerAccountPublic;
    }

    public void setIssuerAccountPublic(String issuerAccountPublic) {
        this.issuerAccountPublic = issuerAccountPublic;
    }

    public List<JpaVoter> getVoters() {
        return voters;
    }

    public void setVoters(List<JpaVoter> voters) {
        this.voters = voters;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean getOnTestNetwork() {
        return isOnTestNetwork;
    }

    public void setOnTestNetwork(Boolean onTestNetwork) {
        isOnTestNetwork = onTestNetwork;
    }

    public boolean isDistributionRefunded() {
        return isDistributionRefunded;
    }

    public void setDistributionRefunded(boolean distributionRefunded) {
        isDistributionRefunded = distributionRefunded;
    }

    public String getUserGivenFundingAccountPublic() {
        return userGivenFundingAccountPublic;
    }

    public void setUserGivenFundingAccountPublic(String userGivenFundingAccountPublic) {
        this.userGivenFundingAccountPublic = userGivenFundingAccountPublic;
    }

    public String getUserGivenFundingAccountSecret() {
        return userGivenFundingAccountSecret;
    }

    public void setUserGivenFundingAccountSecret(String userGivenFundingAccountSecret) {
        this.userGivenFundingAccountSecret = userGivenFundingAccountSecret;
    }

    public boolean isInternalFundingRefunded() {
        return isInternalFundingRefunded;
    }

    public void setInternalFundingRefunded(boolean internalFundingRefunded) {
        isInternalFundingRefunded = internalFundingRefunded;
    }

    public boolean isAuthTokenBased() {
        return isAuthTokenBased;
    }

    public void setAuthTokenBased(boolean authTokenBased) {
        isAuthTokenBased = authTokenBased;
    }

    public boolean isAuthTokensNeedToBeCreated() {
        return isAuthTokensNeedToBeCreated;
    }

    public void setAuthTokensNeedToBeCreated(boolean authTokensNeedToBeCreated) {
        isAuthTokensNeedToBeCreated = authTokensNeedToBeCreated;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public BallotType getBallotType() {
        return ballotType;
    }

    public void setBallotType(BallotType ballotType) {
        this.ballotType = ballotType;
    }

    public Integer getMaxChoices() {
        return maxChoices;
    }

    public void setMaxChoices(Integer maxChoices) {
        this.maxChoices = maxChoices;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
