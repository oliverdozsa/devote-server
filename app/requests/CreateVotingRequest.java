package requests;

import com.typesafe.config.Config;
import play.data.validation.Constraints;
import utils.StringUtils;
import validation.ValidatableWithConfig;
import validation.ValidateWithConfig;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@ValidateWithConfig
public class CreateVotingRequest implements ValidatableWithConfig<String> {
    @Constraints.Required
    @Constraints.MinLength(2)
    private String network;

    @Constraints.Required
    @Constraints.Min(2)
    private Long votesCap;

    @Constraints.Required
    @Constraints.MinLength(2)
    @Constraints.MaxLength(1000)
    private String title;

    @Constraints.Pattern("^[0-9a-z]+$")
    @Constraints.MaxLength(8)
    @Constraints.MinLength(2)
    private String tokenIdentifier;

    private Instant encryptedUntil;

    @Constraints.Required
    private Instant startDate;

    @Constraints.Required
    private Instant endDate;

    private Authorization authorization;

    private List<@Constraints.Email String> authorizationEmailOptions;

    @Constraints.Required
    @Valid
    @Size(min = 1, max = 99)
    private List<CreatePollRequest> polls;

    @Constraints.Required
    private Visibility visibility;

    @Constraints.Required
    private String fundingAccountPublic;

    @Constraints.Required
    private String fundingAccountSecret;

    private Boolean useTestnet;

    private boolean sendInvites;

    private String organizer;

    private BallotType ballotType = BallotType.MULTI_POLL;

    private Integer maxChoices;

    @Constraints.MaxLength(1000)
    private String description;

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public Long getVotesCap() {
        return votesCap;
    }

    public void setVotesCap(Long votesCap) {
        this.votesCap = votesCap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTokenIdentifier() {
        return tokenIdentifier;
    }

    public void setTokenIdentifier(String tokenIdentifier) {
        this.tokenIdentifier = tokenIdentifier;
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

    public List<String> getAuthorizationEmailOptions() {
        return authorizationEmailOptions;
    }

    public void setAuthorizationEmailOptions(List<String> authorizationEmailOptions) {
        this.authorizationEmailOptions = authorizationEmailOptions;
    }

    public List<CreatePollRequest> getPolls() {
        return polls;
    }

    public void setPolls(List<CreatePollRequest> polls) {
        this.polls = polls;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
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

    public boolean isSendInvites() {
        return sendInvites;
    }

    public void setSendInvites(boolean sendInvites) {
        this.sendInvites = sendInvites;
    }

    public Boolean getUseTestnet() {
        return useTestnet;
    }

    public void setUseTestnet(Boolean useTestnet) {
        this.useTestnet = useTestnet;
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

    @Override
    public String validate(Config config) {
        Integer minTimeInterval = config.getInt("galactic.host.vote.vote.related.min.time.interval.sec");

        if (isEncryptedNotValid(minTimeInterval)) {
            return "If encryption is needed, encrypted until must be at least " +
                    minTimeInterval + " seconds from now!";
        }

        if (isAuthorizationNotValid()) {
            return "Invalid authorization! If EMAILS is given, check email addresses!";
        }

        if (isStartEndDateNotValid(minTimeInterval)) {
            return "The minimum difference between start and end date should be " +
                    minTimeInterval + " seconds, and voting must end in the future!";
        }

        long maxVotesCap = config.getLong("galactic.host.vote.max.votes.cap");
        if(votesCap > maxVotesCap) {
            return "Requested votes cap (" + maxVotesCap + ") is greater, than the maximum allowed (" + maxVotesCap + ")";
        }

        if(isSendInvites() && (organizer == null || organizer.length() == 0)) {
            return "Invites are requested, and in this case, organizer cannot be empty!";
        }

        if(ballotType == BallotType.MULTI_CHOICE && (maxChoices == null || maxChoices < 1)) {
            return "If ballot type is MULTI_CHOICE, max choices must be >= 1!";
        }

        return null;
    }

    private boolean isEncryptedNotValid(Integer minTimeIntervalSec) {
        if (encryptedUntil == null) {
            return false;
        }

        Instant now = Instant.now();
        long gapSecs = Duration.between(now, encryptedUntil).toMillis() / 1000;

        return gapSecs < minTimeIntervalSec;
    }

    private boolean isAuthorizationNotValid() {
        return false;
    }

    private boolean isStartEndDateNotValid(Integer minTimeIntervalSecs) {
        if (startDate == null || endDate == null) {
            return true;
        }

        long gapMillis = Duration.between(startDate, endDate).toMillis();
        long minTimeIntervalMillis = minTimeIntervalSecs * 1000;

        Instant now = Instant.now();
        boolean doesNotEndInFuture = this.endDate.isBefore(now);

        return gapMillis < minTimeIntervalMillis || doesNotEndInFuture;
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

    @Override
    public String toString() {
        return "CreateVotingRequest{" +
                "network='" + network + '\'' +
                ", votesCap=" + votesCap +
                ", title='" + title + '\'' +
                ", tokenIdentifier='" + tokenIdentifier + '\'' +
                ", encryptedUntil=" + encryptedUntil +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", authorization=" + authorization +
                ", authorizationEmailOptions=" + authorizationEmailOptions +
                ", polls=" + polls +
                ", visibility=" + visibility +
                ", fundingAccountPublic='" + fundingAccountPublic + '\'' +
                ", fundingAccountSecret='" + StringUtils.redactWithEllipsis(fundingAccountSecret, 5) + '\'' +
                ", useTestnet=" + useTestnet +
                ", sendInvites=" + sendInvites +
                ", organizer='" + organizer + '\'' +
                ", ballotType=" + ballotType +
                ", maxChoices=" + maxChoices +
                ", description='" + description + '\'' +
                '}';
    }

    public enum Authorization {
        EMAILS
    }

    public enum Visibility {
        PUBLIC,
        UNLISTED,
        PRIVATE
    }

    public enum BallotType {
        MULTI_POLL,
        MULTI_CHOICE
    }
}
