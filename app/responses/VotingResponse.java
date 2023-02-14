package responses;

import java.time.Instant;
import java.util.List;

public class VotingResponse {
    private String id;
    private String network;
    private String title;
    private Long votesCap;
    private List<VotingPollResponse> polls;
    private Instant createdAt;
    private Instant encryptedUntil;
    private String decryptionKey;
    private Instant startDate;
    private Instant endDate;
    private String fundingAccountId;
    private String distributionAccountId;
    private String ballotAccountId;
    private String issuerAccountId;
    private String assetCode;
    private String authorization;
    private String visibility;
    private boolean isRefunded;
    private boolean isInvitesBased;
    private String ballotType;

    private boolean isOnTestNetwork;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getIssuerAccountId() {
        return issuerAccountId;
    }

    public void setIssuerAccountId(String issuerAccountId) {
        this.issuerAccountId = issuerAccountId;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public String getDecryptionKey() {
        return decryptionKey;
    }

    public void setDecryptionKey(String decryptionKey) {
        this.decryptionKey = decryptionKey;
    }

    public boolean isOnTestNetwork() {
        return isOnTestNetwork;
    }

    public void setOnTestNetwork(boolean onTestNetwork) {
        isOnTestNetwork = onTestNetwork;
    }

    public String getFundingAccountId() {
        return fundingAccountId;
    }

    public void setFundingAccountId(String fundingAccountId) {
        this.fundingAccountId = fundingAccountId;
    }

    public boolean isRefunded() {
        return isRefunded;
    }

    public void setRefunded(boolean refunded) {
        isRefunded = refunded;
    }

    public boolean isInvitesBased() {
        return isInvitesBased;
    }

    public void setInvitesBased(boolean invitesBased) {
        isInvitesBased = invitesBased;
    }

    public String getBallotType() {
        return ballotType;
    }

    public void setBallotType(String ballotType) {
        this.ballotType = ballotType;
    }
}
