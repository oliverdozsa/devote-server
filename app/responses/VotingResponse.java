package responses;

import java.time.Instant;
import java.util.List;

public class VotingResponse {
    private Long id;
    private String network;
    private String title;
    private Long votesCap;
    private List<VotingPollResponse> polls;
    private List<VotingIssuerResponse> issuers;
    private Instant createdAt;
    private Instant encryptedUntil;
    private Instant startDate;
    private Instant endDate;
    private String distributionAccountId;
    private String ballotAccountId;
    private String authorization;
    private String authOptionKeybase;
    private String visibility;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getVotesCap() {
        return votesCap;
    }

    public void setVotesCap(Long votesCap) {
        this.votesCap = votesCap;
    }

    public List<VotingPollResponse> getPolls() {
        return polls;
    }

    public void setPolls(List<VotingPollResponse> polls) {
        this.polls = polls;
    }

    public List<VotingIssuerResponse> getIssuers() {
        return issuers;
    }

    public void setIssuers(List<VotingIssuerResponse> issuers) {
        this.issuers = issuers;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
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

    public String getDistributionAccountId() {
        return distributionAccountId;
    }

    public void setDistributionAccountId(String distributionAccountId) {
        this.distributionAccountId = distributionAccountId;
    }

    public String getBallotAccountId() {
        return ballotAccountId;
    }

    public void setBallotAccountId(String ballotAccountId) {
        this.ballotAccountId = ballotAccountId;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getAuthOptionKeybase() {
        return authOptionKeybase;
    }

    public void setAuthOptionKeybase(String authOptionKeybase) {
        this.authOptionKeybase = authOptionKeybase;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    @Override
    public String toString() {
        return "VotingResponse{" +
                "id=" + id +
                ", network='" + network + '\'' +
                ", title='" + title + '\'' +
                ", votesCap=" + votesCap +
                ", polls=" + polls +
                ", issuers=" + issuers +
                ", createdAt=" + createdAt +
                ", encryptedUntil=" + encryptedUntil +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", distributionAccountId='" + distributionAccountId + '\'' +
                ", ballotAccountId='" + ballotAccountId + '\'' +
                ", authorization='" + authorization + '\'' +
                ", authOptionKeybase='" + authOptionKeybase + '\'' +
                ", visibility='" + visibility + '\'' +
                '}';
    }
}
